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

# Get host IP (for external access)
get_host_ip() {
    # Try to get host IP from different sources
    # 1. Use explicit HOST_IP env var if set
    if [ -n "${HOST_IP}" ]; then
        echo "$HOST_IP"
        return
    fi

    # 2. Try to get IP from docker gateway
    GATEWAY_IP=$(ip route | grep default | awk '{print $3}' 2>/dev/null)
    if [ -n "$GATEWAY_IP" ]; then
        echo "$GATEWAY_IP"
        return
    fi

    # 3. Fallback: use docker0 bridge IP
    DOCKER_BRIDGE_IP=$(ip addr show docker0 2>/dev/null | grep "inet " | awk '{print $2}' | cut -d'/' -f1)
    if [ -n "$DOCKER_BRIDGE_IP" ]; then
        echo "$DOCKER_BRIDGE_IP"
        return
    fi

    # 4. Last resort: use container's internal IP (will NOT work for external access)
    CONTAINER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
    echo "$CONTAINER_IP"
}

# Get external accessible address
get_external_endpoint() {
    # 优先使用环境变量配置的外部地址（宿主机IP:Agent映射端口）
    if [ -n "${AGENT_EXTERNAL_PORT}" ]; then
        # 如果配置了外部端口
        if [ "${USE_HOST_GATEWAY:-false}" = "true" ]; then
            # 使用 host.docker.internal（宿主机）访问
            echo "http://host.docker.internal:${AGENT_EXTERNAL_PORT}"
        else
            # 使用宿主机的IP
            HOST_IP=$(get_host_ip)
            echo "http://${HOST_IP}:${AGENT_EXTERNAL_PORT}"
        fi
    elif [ -n "${AGENT_EXTERNAL_HOST}" ]; then
        # 如果配置了外部主机名
        echo "http://${AGENT_EXTERNAL_HOST}:8081"
    else
        # 默认使用Docker内部DNS（仅适用于同Docker网络内通信）
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
        echo "http://${NODE_NAME_INTERNAL}:8081"
    fi
}

# Register node with DOS (using Python)
register_node() {
    echo "[$(date)] 正在注册节点到 DOS..."

    # 获取本机IP
    MACHINE_IP=$(get_host_ip)
    export NODE_MACHINE_IP=$MACHINE_IP

    # 获取外部可访问的Agent地址
    AGENT_ENDPOINT=$(get_external_endpoint)
    export NODE_ENDPOINT=$AGENT_ENDPOINT

    echo "节点注册信息："
    echo "  - Node ID: ${NODE_ID}"
    echo "  - Agent端点: ${AGENT_ENDPOINT}"
    echo "  - 机器IP: ${MACHINE_IP}"

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
    # 获取外部可访问的Agent地址
    AGENT_ENDPOINT=$(get_external_endpoint)
    export NODE_ENDPOINT=$AGENT_ENDPOINT

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

# Create test data file
create_test_data() {
    echo "[$(date)] 创建测试数据文件..."

    # Determine data content based on NODE_ID
    local data_file="/tmp/data_content.csv"

    case "${NODE_ID}" in
        *001)
            # Alice: 8条数据
            cat > "$data_file" << 'EOF'
id,name,age,phone,email
1,张三,28,13800138001,zhangsan@example.com
2,李四,35,13800138002,lisi@example.com
3,王五,42,13800138003,wangwu@example.com
4,赵六,31,13800138004,zhaoliu@example.com
5,孙七,26,13800138005,sunqi@example.com
6,周八,39,13800138006,zhouba@example.com
7,吴九,33,13800138007,wujiu@example.com
8,郑十,45,13800138008,zhengshi@example.com
EOF
            echo "Alice 测试数据已创建 (8条)"
            ;;
        *002)
            # Bob: 20条数据，前5条与Alice相同
            cat > "$data_file" << 'EOF'
id,name,age,phone,email
1,张三,28,13800138001,zhangsan@example.com
2,李四,35,13800138002,lisi@example.com
3,王五,42,13800138003,wangwu@example.com
4,赵六,31,13800138004,zhaoliu@example.com
5,孙七,26,13800138005,sunqi@example.com
16,钱十一,27,13800138016,qian11@example.com
17,孙十二,32,13800138017,sun12@example.com
18,周十三,38,13800138018,zhou13@example.com
19,吴十四,29,13800138019,wu14@example.com
20,郑十五,41,13800138020,zheng15@example.com
21,冯十六,33,13800138021,feng16@example.com
22,陈十七,36,13800138022,chen17@example.com
23,褚十八,44,13800138023,chu18@example.com
24,卫十九,30,13800138024,wei19@example.com
25,蒋二十,28,13800138025,jiang20@example.com
26,沈二十一,34,13800138026,shen21@example.com
27,韩二十二,40,13800138027,han22@example.com
28,杨二十三,31,13800138028,yang23@example.com
29,朱二十四,37,13800138029,zhu24@example.com
30,秦二十五,43,13800138030,qin25@example.com
EOF
            echo "Bob 测试数据已创建 (20条，前5条与Alice相同)"
            ;;
        *)
            # 默认数据
            cat > "$data_file" << 'EOF'
id,name,age,phone,email
1,测试用户1,25,13900000001,test1@example.com
2,测试用户2,30,13900000002,test2@example.com
3,测试用户3,35,13900000003,test3@example.com
EOF
            echo "默认测试数据已创建 (3条)"
            ;;
    esac

    # Copy to /data using root privileges
    echo "[$(date)] 复制数据文件到 /data..."
    cp "$data_file" /data/data.csv
    chmod 666 /data/data.csv

    echo "测试数据文件: /data/data.csv"
    echo "文件内容:"
    cat /data/data.csv
}

# Main
echo "[$(date)] 等待 DOS API 就绪..."
sleep 5

# Set API_URL
export API_URL="${DOS_API_URL}"

# Register the node
register_node

# Create test data
create_test_data

# Start Agent service
start_agent_service

# Send heartbeat in a loop
echo "[$(date)] 开始心跳循环 (间隔: ${HEARTBEAT_INTERVAL}秒)..."
while true; do
    send_heartbeat
    sleep $HEARTBEAT_INTERVAL
done
