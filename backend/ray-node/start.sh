#!/bin/bash
set -e

# Configuration
NODE_ID=${NODE_ID:-"ray-node-$HOSTNAME"}
NODE_NAME=${NODE_NAME:-"RAY节点"}
NODE_MODE=${NODE_MODE:-"RAY"}
RAY_HEAD_ADDRESS=${RAY_HEAD_ADDRESS:-""}
# 内部URL (docker网络内)
DOS_API_URL=${DOS_API_URL:-"http://dos-api:8080"}
# 外部URL (从宿主机访问)
DOS_API_EXTERNAL=${DOS_API_EXTERNAL:-"http://localhost:8082"}
HEARTBEAT_INTERVAL=${HEARTBEAT_INTERVAL:-30}

echo "=========================================="
echo "RAY 节点启动配置"
echo "=========================================="
echo "Node ID: $NODE_ID"
echo "Node Name: $NODE_NAME"
echo "Node Mode: $NODE_MODE"
echo "RAY Head Address: $RAY_HEAD_ADDRESS"
echo "DOS API URL: $DOS_API_URL"
echo "=========================================="

# Register node with DOS (using Python)
register_node() {
    echo "[$(date)] 正在注册节点到 DOS..."
    # Head node has exposed ports, use external URL; workers use internal hostname
    if [ -z "$RAY_HEAD_ADDRESS" ]; then
        export NODE_ENDPOINT="${DOS_API_EXTERNAL:-http://localhost:8082}"
    else
        export NODE_ENDPOINT="http://${HOSTNAME}:6379"
    fi

    python3 << EOF
import requests
import json
import os

api_url = os.environ.get('API_URL', '${DOS_API_URL}')
node_id = os.environ.get('NODE_ID', '${NODE_ID}')
node_name = os.environ.get('NODE_NAME', '${NODE_NAME}')
node_mode = os.environ.get('NODE_MODE', '${NODE_MODE}')
endpoint = os.environ.get('NODE_ENDPOINT', 'unknown')

url = f"{api_url}/api/dos/privacy/node/register"
data = {
    "nodeId": node_id,
    "nodeName": node_name,
    "endpoint": endpoint,
    "nodeMode": node_mode
}

try:
    response = requests.post(url, json=data, timeout=10)
    print(f"注册响应: {response.text}")
except Exception as e:
    print(f"注册失败: {e}")
EOF
}

# Send heartbeat to DOS (using Python)
send_heartbeat() {
    # Head node has exposed ports, use external URL; workers use internal hostname
    if [ -z "$RAY_HEAD_ADDRESS" ]; then
        export NODE_ENDPOINT="${DOS_API_EXTERNAL:-http://localhost:8082}"
    else
        export NODE_ENDPOINT="http://${HOSTNAME}:6379"
    fi

    python3 << EOF
import requests
import os

api_url = os.environ.get('API_URL', '${DOS_API_URL}')
node_id = os.environ.get('NODE_ID', '${NODE_ID}')

url = f"{api_url}/api/dos/privacy/node/{node_id}/heartbeat"

try:
    response = requests.post(url, timeout=10)
    if response.status_code == 500 and "Node not found" in response.text:
        # 节点不存在，需要重新注册
        print(f"节点不存在，重新注册...")
        register_url = f"{api_url}/api/dos/privacy/node/register"
        node_name = os.environ.get('NODE_NAME', '${NODE_NAME}')
        node_mode = os.environ.get('NODE_MODE', '${NODE_MODE}')
        endpoint = os.environ.get('NODE_ENDPOINT', 'unknown')
        data = {
            "nodeId": node_id,
            "nodeName": node_name,
            "endpoint": endpoint,
            "nodeMode": node_mode
        }
        reg_response = requests.post(register_url, json=data, timeout=10)
        print(f"重新注册响应: {reg_response.text}")
    else:
        print(f"心跳响应: {response.text}")
except Exception as e:
    print(f"心跳失败: {e}")
EOF
}

# Start Ray based on role
start_ray_node() {
    if [ -z "$RAY_HEAD_ADDRESS" ]; then
        # This is the head node
        echo "[$(date)] 启动 RAY Head 节点..."
        ray start --head --port=6379 --dashboard-host=0.0.0.0 --include-dashboard=true &
    else
        # This is a worker node
        echo "[$(date)] 启动 RAY Worker 节点，连接到: $RAY_HEAD_ADDRESS"
        ray start --address="$RAY_HEAD_ADDRESS" &
    fi
}

# Main
echo "[$(date)] 等待 DOS API 就绪..."
sleep 5

# Set API_URL based on connectivity
export API_URL="${DOS_API_URL}"

# Register the node
register_node

# Start Ray
start_ray_node

# Wait for Ray to be ready
sleep 3

# Send heartbeat in a loop
echo "[$(date)] 开始心跳循环 (间隔: ${HEARTBEAT_INTERVAL}秒)..."
while true; do
    send_heartbeat
    sleep $HEARTBEAT_INTERVAL
done
