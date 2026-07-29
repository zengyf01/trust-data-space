#!/bin/bash
set -e

# Configuration
NODE_ID=${NODE_ID:-"ray-node-$HOSTNAME"}
NODE_NAME=${NODE_NAME:-"RAY节点"}
NODE_MODE=${NODE_MODE:-"RAY"}
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
echo "DOS API URL: $DOS_API_URL"
echo "=========================================="

# Get machine IP
get_machine_ip() {
    # Try multiple methods to get the machine IP
    MACHINE_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
    if [ -z "$MACHINE_IP" ]; then
        MACHINE_IP=$(ip route get 1 | awk '{print $(NF); exit}' 2>/dev/null)
    fi
    if [ -z "$MACHINE_IP" ]; then
        MACHINE_IP="unknown"
    fi
    echo "$MACHINE_IP"
}

# Register node with DOS (using Python)
register_node() {
    echo "[$(date)] 正在注册节点到 DOS..."

    # 根据节点名称确定Agent内部访问地址
    # DOS API在Docker网络内，使用容器主机名:8081访问Agent
    case "${NODE_ID}" in
        *001)
            NODE_NAME_INTERNAL="ray-node-1"
            ;;
        *002)
            NODE_NAME_INTERNAL="ray-node-2"
            ;;
        *003)
            NODE_NAME_INTERNAL="ray-node-3"
            ;;
        *)
            NODE_NAME_INTERNAL="ray-node-1"
            ;;
    esac
    export NODE_ENDPOINT="http://${NODE_NAME_INTERNAL}:8081"

    MACHINE_IP=$(get_machine_ip)
    export NODE_MACHINE_IP=$MACHINE_IP

    python3 << EOF
import requests
import json
import os

api_url = os.environ.get('API_URL', '${DOS_API_URL}')
node_id = os.environ.get('NODE_ID', '${NODE_ID}')
node_name = os.environ.get('NODE_NAME', '${NODE_NAME}')
node_mode = os.environ.get('NODE_MODE', '${NODE_MODE}')
endpoint = os.environ.get('NODE_ENDPOINT', 'unknown')
machine_ip = os.environ.get('NODE_MACHINE_IP', 'unknown')

url = f"{api_url}/api/dos/privacy/node/register"
data = {
    "nodeId": node_id,
    "nodeName": node_name,
    "endpoint": endpoint,
    "nodeMode": node_mode,
    "machineIp": machine_ip,
    "rayPort": 6379
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
    # 根据节点名称确定Agent内部访问地址
    case "${NODE_ID}" in
        *001)
            NODE_NAME_INTERNAL="ray-node-1"
            ;;
        *002)
            NODE_NAME_INTERNAL="ray-node-2"
            ;;
        *003)
            NODE_NAME_INTERNAL="ray-node-3"
            ;;
        *)
            NODE_NAME_INTERNAL="ray-node-1"
            ;;
    esac
    export NODE_ENDPOINT="http://${NODE_NAME_INTERNAL}:8081"

    python3 << EOF
import requests
import os

api_url = os.environ.get('API_URL', '${DOS_API_URL}')
node_id = os.environ.get('NODE_ID', '${NODE_ID}')
endpoint = os.environ.get('NODE_ENDPOINT', 'unknown')

url = f"{api_url}/api/dos/privacy/node/{node_id}/heartbeat"

try:
    response = requests.post(url, timeout=10)
    if response.status_code == 500 and "Node not found" in response.text:
        # 节点不存在，需要重新注册
        print(f"节点不存在，重新注册...")
        register_url = f"{api_url}/api/dos/privacy/node/register"
        node_name = os.environ.get('NODE_NAME', '${NODE_NAME}')
        node_mode = os.environ.get('NODE_MODE', '${NODE_MODE}')
        machine_ip = os.environ.get('NODE_MACHINE_IP', 'unknown')
        data = {
            "nodeId": node_id,
            "nodeName": node_name,
            "endpoint": endpoint,
            "nodeMode": node_mode,
            "machineIp": machine_ip,
            "rayPort": 6379
        }
        reg_response = requests.post(register_url, json=data, timeout=10)
        print(f"重新注册响应: {reg_response.text}")
    else:
        print(f"心跳响应: {response.text}")
except Exception as e:
    print(f"心跳失败: {e}")
EOF
}

# Start Agent service
start_agent_service() {
    echo "[$(date)] 启动 Agent 服务..."
    # 复制agent脚本并启动
    cp /app/agent.py /tmp/agent.py 2>/dev/null || true
    python3 /app/agent.py &
    echo "Agent服务已启动，监听端口 8081"
}

# Main
echo "[$(date)] 等待 DOS API 就绪..."
sleep 5

# Set API_URL
export API_URL="${DOS_API_URL}"

# Register the node
register_node

# Start Agent service
start_agent_service

# Send heartbeat in a loop
echo "[$(date)] 开始心跳循环 (间隔: ${HEARTBEAT_INTERVAL}秒)..."
while true; do
    send_heartbeat
    sleep $HEARTBEAT_INTERVAL
done
