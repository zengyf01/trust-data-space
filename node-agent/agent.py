#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
节点Agent服务 - Ray集群管理
监听HTTP请求，管理本地Ray进程和任务执行
"""

import os
import sys
import json
import uuid
import subprocess
import threading
import time
import logging
from datetime import datetime
from flask import Flask, request, jsonify
import requests

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# 全局状态
class State:
    ray_process = None
    ray_head_address = None
    is_head = False
    current_cluster_id = None
    tasks = {}  # task_id -> TaskInfo

class TaskInfo:
    def __init__(self, job_id, script, task_id, status='PENDING', result=None, error=None):
        self.job_id = job_id
        self.script = script
        self.task_id = task_id
        self.status = status
        self.result = result
        self.error = error
        self.start_time = datetime.now()
        self.process = None

state = State()

# ==================== Ray管理接口 ====================

@app.route('/agent/ray/start-head', methods=['POST'])
def start_head():
    """启动Ray Head节点"""
    try:
        data = request.get_json() or {}
        ray_port = data.get('rayPort', 10001)
        dashboard_port = data.get('dashboardPort', 8265)

        # 如果已有Ray进程在运行，先停止
        if state.ray_process is not None:
            stop_ray()

        # 获取本机IP
        host_ip = get_local_ip()
        if not host_ip:
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 启动Ray Head
        cmd = [
            'ray', 'start', '--head',
            '--port', str(ray_port),
            '--dashboard-port', str(dashboard_port),
            '--node-ip-address', host_ip
        ]

        logger.info(f"Starting Ray Head: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)

        if result.returncode != 0:
            logger.error(f"Failed to start Ray Head: {result.stderr}")
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Head失败: {result.stderr}'}), 200

        # 等待Ray初始化
        time.sleep(2)

        # 更新状态
        state.ray_head_address = f"ray://{host_ip}:{ray_port}"
        state.is_head = True
        state.current_cluster_id = str(uuid.uuid4())

        logger.info(f"Ray Head started successfully at {state.ray_head_address}")

        return jsonify({
            'code': 200,
            'data': {
                'rayAddress': state.ray_head_address,
                'clusterId': state.current_cluster_id,
                'nodeIp': host_ip
            },
            'msg': 'success'
        }), 200

    except subprocess.TimeoutExpired:
        logger.error("Ray Head start timeout")
        return jsonify({'code': 500, 'data': None, 'msg': '启动Ray Head超时'}), 200
    except Exception as e:
        logger.error(f"Error starting Ray Head: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/ray/start-worker', methods=['POST'])
def start_worker():
    """启动Ray Worker节点，加入已有集群"""
    try:
        data = request.get_json() or {}
        head_address = data.get('headAddress')
        ray_port = data.get('rayPort', 10001)

        if not head_address:
            return jsonify({'code': 500, 'data': None, 'msg': 'headAddress不能为空'}), 200

        # 如果已有Ray进程在运行，先停止
        if state.ray_process is not None:
            stop_ray()

        # 获取本机IP
        host_ip = get_local_ip()
        if not host_ip:
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 启动Ray Worker
        cmd = [
            'ray', 'start', '--address', head_address,
            '--port', str(ray_port),
            '--node-ip-address', host_ip
        ]

        logger.info(f"Starting Ray Worker: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)

        if result.returncode != 0:
            logger.error(f"Failed to start Ray Worker: {result.stderr}")
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Worker失败: {result.stderr}'}), 200

        # 等待Ray初始化
        time.sleep(2)

        # 更新状态
        state.ray_head_address = head_address
        state.is_head = False

        logger.info(f"Ray Worker started successfully, joined cluster at {head_address}")

        return jsonify({
            'code': 200,
            'data': {
                'status': 'joined',
                'headAddress': head_address,
                'nodeIp': host_ip
            },
            'msg': 'success'
        }), 200

    except subprocess.TimeoutExpired:
        logger.error("Ray Worker start timeout")
        return jsonify({'code': 500, 'data': None, 'msg': '启动Ray Worker超时'}), 200
    except Exception as e:
        logger.error(f"Error starting Ray Worker: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/ray/stop', methods=['POST'])
def stop_ray():
    """停止Ray节点"""
    try:
        if state.ray_process is not None:
            state.ray_process.terminate()
            state.ray_process = None

        # 执行ray stop命令
        logger.info("Stopping Ray...")
        result = subprocess.run(['ray', 'stop'], capture_output=True, text=True, timeout=30)

        if result.returncode != 0:
            logger.warn(f"ray stop returned non-zero: {result.stderr}")

        # 重置状态
        state.ray_head_address = None
        state.is_head = False
        state.current_cluster_id = None

        logger.info("Ray stopped successfully")

        return jsonify({
            'code': 200,
            'data': {'status': 'stopped'},
            'msg': 'success'
        }), 200

    except subprocess.TimeoutExpired:
        logger.error("Ray stop timeout")
        return jsonify({'code': 500, 'data': None, 'msg': '停止Ray超时'}), 200
    except Exception as e:
        logger.error(f"Error stopping Ray: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/ray/status', methods=['GET'])
def get_ray_status():
    """查询Ray状态"""
    try:
        running = is_ray_running()

        return jsonify({
            'code': 200,
            'data': {
                'running': running,
                'clusterId': state.current_cluster_id,
                'rayAddress': state.ray_head_address,
                'isHead': state.is_head,
                'nodeIp': get_local_ip()
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error getting Ray status: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


# ==================== 任务执行接口 ====================

@app.route('/agent/task/run', methods=['POST'])
def run_task():
    """执行Python脚本任务"""
    try:
        data = request.get_json() or {}
        script = data.get('script')
        task_id = data.get('taskId')

        if not script:
            return jsonify({'code': 500, 'data': None, 'msg': 'script不能为空'}), 200

        if not is_ray_running():
            return jsonify({'code': 500, 'data': None, 'msg': 'Ray未运行，请先启动Ray集群'}), 200

        # 生成job_id
        job_id = f"ray-job-{uuid.uuid4().hex[:8]}"

        # 创建任务
        task_info = TaskInfo(job_id, script, task_id, status='SUBMITTED')
        state.tasks[job_id] = task_info

        # 在后台线程执行任务
        thread = threading.Thread(target=execute_script, args=(job_id, script, task_id))
        thread.daemon = True
        thread.start()

        logger.info(f"Task submitted: jobId={job_id}, taskId={task_id}")

        return jsonify({
            'code': 200,
            'data': {
                'jobId': job_id,
                'status': 'SUBMITTED'
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error running task: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/task/status/<job_id>', methods=['GET'])
def get_task_status(job_id):
    """查询任务状态"""
    try:
        task_info = state.tasks.get(job_id)

        if task_info is None:
            return jsonify({'code': 404, 'data': None, 'msg': '任务不存在'}), 200

        return jsonify({
            'code': 200,
            'data': {
                'jobId': job_id,
                'taskId': task_info.task_id,
                'status': task_info.status,
                'result': task_info.result,
                'error': task_info.error,
                'startTime': task_info.start_time.isoformat() if task_info.start_time else None
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error getting task status: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/task/stop/<job_id>', methods=['POST'])
def stop_task(job_id):
    """停止任务"""
    try:
        task_info = state.tasks.get(job_id)

        if task_info is None:
            return jsonify({'code': 404, 'data': None, 'msg': '任务不存在'}), 200

        if task_info.process is not None:
            task_info.process.terminate()
            task_info.status = 'STOPPED'
            logger.info(f"Task {job_id} stopped by user")
        else:
            task_info.status = 'STOPPED'

        return jsonify({
            'code': 200,
            'data': {'status': 'stopped'},
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error stopping task: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


# ==================== 节点上报接口 ====================

@app.route('/agent/register', methods=['POST'])
def register():
    """节点注册到DOS平台"""
    try:
        data = request.get_json() or {}
        node_id = data.get('nodeId')
        node_name = data.get('nodeName')
        machine_ip = data.get('machineIp')
        ray_port = data.get('rayPort', 10001)

        logger.info(f"Node registered: {node_id}, {node_name}, {machine_ip}:{ray_port}")

        return jsonify({
            'code': 200,
            'data': {
                'registered': True,
                'agentPort': 8081
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error registering node: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/heartbeat', methods=['POST'])
def heartbeat():
    """节点心跳"""
    try:
        data = request.get_json() or {}
        node_id = data.get('nodeId')
        status = data.get('status', 'IDLE')

        return jsonify({
            'code': 200,
            'data': {
                'clusterId': state.current_cluster_id,
                'rayRunning': is_ray_running()
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f"Error processing heartbeat: {e}")
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


# ==================== 健康检查 ====================

@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({'status': 'ok', 'timestamp': datetime.now().isoformat()}), 200


# ==================== 辅助函数 ====================

def is_ray_running():
    """检查Ray是否在运行"""
    try:
        result = subprocess.run(['ray', 'status'], capture_output=True, text=True, timeout=5)
        return result.returncode == 0
    except:
        return False


def get_local_ip():
    """获取本机IP地址"""
    try:
        import socket
        # 连接外部地址来获取本机IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        # 备用方案
        try:
            import socket
            hostname = socket.gethostname()
            ip = socket.gethostbyname(hostname)
            return ip
        except:
            return None


def execute_script(job_id, script, task_id):
    """在新线程中执行Python脚本"""
    task_info = state.tasks.get(job_id)
    if task_info is None:
        return

    task_info.status = 'RUNNING'
    task_info.process = subprocess.Popen(
        ['python3', '-c', script],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )

    try:
        stdout, stderr = task_info.process.communicate(timeout=300)  # 5分钟超时

        if task_info.process.returncode == 0:
            task_info.status = 'SUCCEEDED'
            task_info.result = stdout
            logger.info(f"Task {job_id} completed successfully")
        else:
            task_info.status = 'FAILED'
            task_info.error = stderr
            logger.error(f"Task {job_id} failed: {stderr}")

    except subprocess.TimeoutExpired:
        task_info.process.kill()
        task_info.status = 'FAILED'
        task_info.error = 'Task execution timeout (5 minutes)'
        logger.error(f"Task {job_id} timeout")
    except Exception as e:
        task_info.status = 'FAILED'
        task_info.error = str(e)
        logger.error(f"Task {job_id} error: {e}")
    finally:
        task_info.process = None


if __name__ == '__main__':
    # 默认端口8081
    port = int(os.environ.get('AGENT_PORT', 8081))
    logger.info(f"Starting Node Agent on port {port}")
    app.run(host='0.0.0.0', port=port, debug=False, threaded=True)
