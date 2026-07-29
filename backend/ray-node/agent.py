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
import socket
from datetime import datetime
from flask import Flask, request, jsonify

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
    ray_address = None  # 当前节点的Ray地址（Worker节点用）
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
        ray_port = data.get('rayPort', 6379)  # Ray 2.x默认GCS端口是6379
        dashboard_port = data.get('dashboardPort', 8265)

        # 如果已有Ray进程在运行，先停止
        if state.ray_process is not None:
            stop_ray()

        # 获取本机IP（容器内需要通过hostname -I 获取）
        host_ip = get_local_ip()
        if not host_ip:
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 先停止可能存在的Ray进程
        subprocess.run(['ray', 'stop', '--force'], capture_output=True, timeout=30)
        time.sleep(1)

        # 启动Ray Head
        cmd = [
            'ray', 'start', '--head',
            '--port', str(ray_port),
            '--dashboard-port', str(dashboard_port),
            '--node-ip-address', host_ip,
            '--disable-usage-stats'
        ]

        logger.info(f"Starting Ray Head: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)

        if result.returncode != 0:
            logger.error(f"Failed to start Ray Head: {result.stderr}")
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Head失败: {result.stderr}'}), 200

        # 等待Ray初始化
        time.sleep(3)

        # 更新状态
        head_address = f"ray://{host_ip}:{ray_port}"
        state.ray_head_address = head_address
        state.ray_address = head_address  # Head节点自己的Ray地址
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

        # 先停止可能存在的Ray进程
        subprocess.run(['ray', 'stop', '--force'], capture_output=True, timeout=30)
        time.sleep(1)

        # 获取本机IP
        host_ip = get_local_ip()
        if not host_ip:
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 启动Ray Worker
        # head_address格式是 ray://172.168.1.1:6379，需要提取出 172.168.1.1:6379
        worker_address = head_address
        if worker_address.startswith('ray://'):
            worker_address = worker_address[6:]  # 跳过 ray:// 前缀

        cmd = [
            'ray', 'start', '--address', worker_address,
            '--node-ip-address', host_ip,
            '--disable-usage-stats'
        ]

        logger.info(f"Starting Ray Worker: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)

        if result.returncode != 0:
            logger.error(f"Failed to start Ray Worker: {result.stderr}")
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Worker失败: {result.stderr}'}), 200

        # 等待Ray初始化
        time.sleep(3)

        # 计算Worker自己的Ray地址
        worker_ray_address = f"ray://{host_ip}:{ray_port}"

        # 更新状态
        state.ray_head_address = head_address
        state.ray_address = worker_ray_address  # 保存Worker自己的地址
        state.is_head = False

        logger.info(f"Ray Worker started successfully, joined cluster at {head_address}, worker address: {worker_ray_address}")

        return jsonify({
            'code': 200,
            'data': {
                'status': 'joined',
                'headAddress': head_address,
                'nodeIp': host_ip,
                'rayAddress': worker_ray_address
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
        # 容器内获取IP的方式
        result = subprocess.run(
            ['bash', '-c', "hostname -I | awk '{print $1}'"],
            capture_output=True, text=True, timeout=5
        )
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    except:
        pass

    try:
        # 备用方案
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        pass

    try:
        hostname = socket.gethostname()
        ip = socket.gethostbyname(hostname)
        return ip
    except:
        return None


def execute_script(job_id, script, task_id):
    """在新线程中执行Python脚本 - 提交到Ray集群执行"""
    task_info = state.tasks.get(job_id)
    if task_info is None:
        return

    task_info.status = 'RUNNING'

    # 1. 将脚本写入临时文件
    script_path = f'/tmp/ray_job_{job_id}.py'
    try:
        with open(script_path, 'w', encoding='utf-8') as f:
            f.write(script)
        logger.info(f"Script written to {script_path}")
    except Exception as e:
        task_info.status = 'FAILED'
        task_info.error = f"Failed to write script: {e}"
        logger.error(f"Task {job_id} failed to write script: {e}")
        return

    # 2. 检查Ray是否在运行
    if not is_ray_running():
        task_info.status = 'FAILED'
        task_info.error = 'Ray is not running on this node'
        logger.error(f"Task {job_id} failed: Ray not running")
        return

    # 3. 获取Ray Head地址
    ray_address = state.ray_head_address
    if not ray_address:
        task_info.status = 'FAILED'
        task_info.error = 'Ray head address not found'
        logger.error(f"Task {job_id} failed: no Ray head address")
        return

    # 4. 使用 ray job submit 提交到Ray集群
    # ray job submit --address {ray_address} -- python {script_path}
    # 注意：ray_address 格式是 ray://host:port，需要转换为 host:port
    ray_address_for_cli = ray_address
    if ray_address_for_cli.startswith('ray://'):
        ray_address_for_cli = ray_address_for_cli[6:]  # 去掉 ray:// 前缀

    cmd = [
        'ray', 'job', 'submit',
        '--address', ray_address_for_cli,
        '--', 'python', script_path
    ]

    logger.info(f"Submitting job to Ray: {' '.join(cmd)}")

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=300  # 5分钟超时
        )

        # 解析提交结果
        if result.returncode == 0:
            # ray job submit 成功后会返回 job_id
            submitted_job_id = result.stdout.strip()
            task_info.result = f"Job submitted successfully: {submitted_job_id}"
            logger.info(f"Task {job_id} submitted to Ray, job_id: {submitted_job_id}")

            # 等待作业完成
            wait_cmd = [
                'ray', 'job', 'status',
                '--address', ray_address_for_cli,
                submitted_job_id
            ]

            # 轮询作业状态
            for _ in range(60):  # 最多等60次（5分钟）
                time.sleep(5)
                status_result = subprocess.run(wait_cmd, capture_output=True, text=True)
                if status_result.returncode == 0:
                    status_output = status_result.stdout.strip().upper()
                    logger.info(f"Job {submitted_job_id} status: {status_output}")

                    if 'SUCCEEDED' in status_output or 'SUCCESS' in status_output:
                        task_info.status = 'SUCCEEDED'
                        # 获取作业日志
                        logs_cmd = ['ray', 'job', 'logs', '--address', ray_address_for_cli, submitted_job_id]
                        logs_result = subprocess.run(logs_cmd, capture_output=True, text=True)
                        task_info.result = logs_result.stdout if logs_result.returncode == 0 else result.stdout
                        break
                    elif 'FAILED' in status_output or 'ERROR' in status_output:
                        task_info.status = 'FAILED'
                        task_info.error = f"Ray job failed: {status_output}"
                        # 获取错误日志
                        logs_cmd = ['ray', 'job', 'logs', '--address', ray_address_for_cli, submitted_job_id]
                        logs_result = subprocess.run(logs_cmd, capture_output=True, text=True)
                        task_info.error += f"\nLogs: {logs_result.stderr}"
                        break
                time.sleep(1)
            else:
                # 超时
                task_info.status = 'FAILED'
                task_info.error = 'Job execution timeout (5 minutes)'
                logger.error(f"Task {job_id} timeout")

        else:
            task_info.status = 'FAILED'
            task_info.error = f"Job submission failed: {result.stderr}"
            logger.error(f"Task {job_id} submission failed: {result.stderr}")

    except subprocess.TimeoutExpired:
        task_info.status = 'FAILED'
        task_info.error = 'Job submission timeout'
        logger.error(f"Task {job_id} submission timeout")
    except Exception as e:
        task_info.status = 'FAILED'
        task_info.error = str(e)
        logger.error(f"Task {job_id} error: {e}")
    finally:
        # 清理临时文件
        try:
            if os.path.exists(script_path):
                os.remove(script_path)
        except:
            pass


if __name__ == '__main__':
    # 默认端口8081
    port = int(os.environ.get('AGENT_PORT', 8081))
    logger.info(f"Starting Node Agent on port {port}")
    app.run(host='0.0.0.0', port=port, debug=False, threaded=True)
