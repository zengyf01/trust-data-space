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
from flask import Flask, request, jsonify, Response

# 配置日志
# 配置日志 - 中文
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)
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
            logger.error('无法获取本机IP地址')
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 先停止可能存在的Ray进程
        logger.info('正在停止可能存在的Ray进程...')
        subprocess.run(['ray', 'stop', '--force'], capture_output=True, timeout=30)
        time.sleep(1)

        # 启动Ray Head
        # Ray 2.x: --port 设置 client_server 端口，--gcs-server-port 设置 gcs_server 端口
        # worker_ports 不能与 gcs_server_port 和 client_server_port 冲突
        gcs_server_port = ray_port
        client_server_port = ray_port + 1  # 与 gcs_server_port 错开
        # worker_port_start 从 ray_port + 2 开始，避免冲突
        worker_port_start = ray_port + 2
        cmd = [
            'ray', 'start', '--head',
            '--port', str(client_server_port),
            '--gcs-server-port', str(gcs_server_port),
            '--node-ip-address', host_ip,
            '--min-worker-port', str(worker_port_start),
            '--max-worker-port', '19999',
            '--disable-usage-stats'
        ]

        logger.info(f'启动Ray Head命令: {" ".join(cmd)}')
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)

        if result.returncode != 0:
            logger.error(f'启动Ray Head失败: {result.stderr}')
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Head失败: {result.stderr}'}), 200

        # 等待Ray初始化
        time.sleep(3)

        # 更新状态
        head_address = f"ray://{host_ip}:{ray_port}"
        state.ray_head_address = head_address
        state.ray_address = head_address  # Head节点自己的Ray地址
        state.is_head = True
        state.current_cluster_id = str(uuid.uuid4())

        logger.info(f'Ray Head启动成功，地址: {state.ray_head_address}')

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
            logger.error('Worker启动失败: headAddress不能为空')
            return jsonify({'code': 500, 'data': None, 'msg': 'headAddress不能为空'}), 200

        # 如果已有Ray进程在运行，先停止
        if state.ray_process is not None:
            stop_ray()

        # 先停止可能存在的Ray进程
        logger.info('正在停止可能存在的Ray进程...')
        subprocess.run(['ray', 'stop', '--force'], capture_output=True, timeout=30)
        time.sleep(1)

        # 获取本机IP
        host_ip = get_local_ip()
        if not host_ip:
            logger.error('无法获取本机IP地址')
            return jsonify({'code': 500, 'data': None, 'msg': '无法获取本机IP'}), 200

        # 启动Ray Worker
        # head_address格式是 ray://172.168.1.1:10001，需要转换为GCS端口 172.168.1.1:10002
        # Ray 2.x 客户端端口=10001，GCS端口=10002，Worker需要连接GCS端口
        worker_address = head_address
        if worker_address.startswith('ray://'):
            worker_address = worker_address[6:]  # 跳过 ray:// 前缀
            # Ray 2.x: 客户端端口是 GCS端口+1，改为直接连接 GCS 端口
            parts = worker_address.rsplit(':', 1)
            if len(parts) == 2:
                try:
                    client_port = int(parts[1])
                    gcs_port = client_port + 1  # 10001 → 10002
                    worker_address = f"{parts[0]}:{gcs_port}"
                    logger.info(f'转换Worker连接地址: {head_address} → {worker_address} (GCS端口)')
                except ValueError:
                    pass  # 端口不是数字，保持原样

        cmd = [
            'ray', 'start', '--address', worker_address,
            '--node-ip-address', host_ip,
            '--disable-usage-stats'
        ]

        logger.info(f'启动Ray Worker命令: {" ".join(cmd)}')
        # 使用后台执行避免卡住，subprocess.run 会等待60s超时
        proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        try:
            stdout, stderr = proc.communicate(timeout=60)
            result_returncode = proc.returncode
            result_stdout = stdout.decode() if stdout else ''
            result_stderr = stderr.decode() if stderr else ''
        except subprocess.TimeoutExpired:
            proc.kill()
            stdout, stderr = proc.communicate()
            logger.error(f'启动Ray Worker超时，已kill进程')
            return jsonify({'code': 500, 'data': None, 'msg': '启动Ray Worker超时'}), 200

        if result_returncode != 0:
            logger.error(f'启动Ray Worker失败: {result_stderr}')
            return jsonify({'code': 500, 'data': None, 'msg': f'启动Ray Worker失败: {result_stderr}'}), 200

        # 等待Ray初始化
        time.sleep(3)

        # 计算Worker自己的Ray地址
        worker_ray_address = f"ray://{host_ip}:{ray_port}"

        # 更新状态
        state.ray_head_address = head_address
        state.ray_address = worker_ray_address  # 保存Worker自己的地址
        state.is_head = False

        logger.info(f'Ray Worker启动成功，已加入集群: {head_address}，本节点地址: {worker_ray_address}')

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
        logger.info(f'查询Ray状态: 运行中={running}, 集群ID={state.current_cluster_id}, Ray地址={state.ray_head_address}')

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
        logger.error(f'查询Ray状态失败: {e}')
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
            logger.error('任务提交失败: script不能为空')
            return jsonify({'code': 500, 'data': None, 'msg': 'script不能为空'}), 200

        if not is_ray_running():
            logger.error('任务提交失败: Ray未运行，请先启动Ray集群')
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

        logger.info(f'任务已提交: jobId={job_id}, taskId={task_id}')

        return jsonify({
            'code': 200,
            'data': {
                'jobId': job_id,
                'status': 'SUBMITTED'
            },
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f'任务提交失败: {e}')
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/task/status/<job_id>', methods=['GET'])
def get_task_status(job_id):
    """查询任务状态"""
    try:
        task_info = state.tasks.get(job_id)

        if task_info is None:
            logger.warn(f'查询任务状态: 任务不存在 jobId={job_id}')
            return jsonify({'code': 404, 'data': None, 'msg': '任务不存在'}), 200

        logger.info(f'任务状态查询: jobId={job_id}, status={task_info.status}')
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
        logger.error(f'查询任务状态失败: {e}')
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/task/file', methods=['GET'])
def get_task_file():
    """按 job_id + 路径下载任务输出文件。

    任务脚本负责把输出路径回传到 DOS（通过 TDS_PSI_RESULT 摘要里的 outputPath），
    DOS 再用本接口把容器内 /tmp 下的产物文件取回。
    安全策略：仅允许 /tmp/ 下的普通文件，禁止 .. 路径穿越与符号链接外跳。
    """
    try:
        job_id = request.args.get('jobId', '')
        file_path = request.args.get('path', '')
        if not job_id or not file_path:
            return jsonify({'code': 400, 'data': None, 'msg': 'jobId 与 path 必填'}), 200

        task_info = state.tasks.get(job_id)
        if task_info is None:
            return jsonify({'code': 404, 'data': None, 'msg': '任务不存在'}), 200
        if task_info.status != 'SUCCEEDED':
            return jsonify({'code': 409, 'data': None, 'msg': '任务未成功，当前状态: ' + task_info.status}), 200

        # 路径安全检查
        real_path = os.path.realpath(file_path)
        if not real_path.startswith('/tmp/'):
            return jsonify({'code': 403, 'data': None, 'msg': '仅允许访问 /tmp/ 下的文件'}), 200
        if not os.path.isfile(real_path):
            return jsonify({'code': 404, 'data': None, 'msg': '文件不存在: ' + file_path}), 200

        with open(real_path, 'rb') as f:
            content = f.read()
        logger.info(f'文件下载: jobId={job_id}, path={file_path}, bytes={len(content)}')
        # 直接返回字节流（不再包 JSON）。csv 是文本，按文本返回让 DOS 透传给上层。
        return Response(content, mimetype='text/csv', headers={
            'Content-Disposition': f'attachment; filename="{os.path.basename(real_path)}"'
        })
    except Exception as e:
        logger.error(f'文件下载失败: {e}')
        return jsonify({'code': 500, 'data': None, 'msg': str(e)}), 200


@app.route('/agent/task/stop/<job_id>', methods=['POST'])
def stop_task(job_id):
    """停止任务"""
    try:
        task_info = state.tasks.get(job_id)

        if task_info is None:
            logger.warn(f'停止任务: 任务不存在 jobId={job_id}')
            return jsonify({'code': 404, 'data': None, 'msg': '任务不存在'}), 200

        if task_info.process is not None:
            task_info.process.terminate()
            task_info.status = 'STOPPED'
            logger.info(f'任务已停止: jobId={job_id}')
        else:
            task_info.status = 'STOPPED'
            logger.info(f'任务已标记为停止: jobId={job_id}')

        return jsonify({
            'code': 200,
            'data': {'status': 'stopped'},
            'msg': 'success'
        }), 200

    except Exception as e:
        logger.error(f'停止任务失败: {e}')
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
        logger.error(f'执行脚本失败: 任务不存在 jobId={job_id}')
        return

    task_info.status = 'RUNNING'
    logger.info(f'开始执行任务: jobId={job_id}, taskId={task_id}')

    # 1. 将脚本写入临时文件
    script_path = f'/tmp/ray_job_{job_id}.py'
    try:
        with open(script_path, 'w', encoding='utf-8') as f:
            f.write(script)
        logger.info(f'脚本已写入: {script_path}')
    except Exception as e:
        task_info.status = 'FAILED'
        task_info.error = f'写入脚本失败: {e}'
        logger.error(f'任务 {job_id} 写入脚本失败: {e}')
        return

    # 2. 检查Ray是否在运行
    if not is_ray_running():
        task_info.status = 'FAILED'
        task_info.error = 'Ray未运行，请先启动Ray集群'
        logger.error(f'任务 {job_id} 执行失败: Ray未运行')
        return

    # 3. 获取Ray Head地址
    ray_address = state.ray_head_address
    if not ray_address:
        task_info.status = 'FAILED'
        task_info.error = 'Ray Head地址未找到'
        logger.error(f'任务 {job_id} 执行失败: Ray Head地址未找到')
        return

    # 4. 直接以 Ray Driver 方式运行脚本（绕过 ray job submit 机制）
    # 为什么不用 ray job submit：Ray Job 模式下，worker 的 Python 主线程由 Ray C++ 启动，
    # 不是真正的 main thread，sf.init() → ray.init() 内部 SIGTERM 主线程检查会硬失败
    # 以 Driver 方式运行时，Python 主线程就是真正的 main thread，SF/Ray 初始化正常
    python_exe = sys.executable

    # 设置 Ray Driver 所需的环境变量
    # 脚本内部会调用 sf.init(address='ray://...')，无需在此设置 RAY_ADDRESS
    # 但保留一些 Ray 优化的环境变量
    script_env = os.environ.copy()
    script_env['PYTHONUNBUFFERED'] = '1'  # 实时输出，便于调试
    script_env['RAY_DISABLE_DOCKER_CPU_WARNING'] = '1'

    cmd = [python_exe, script_path]

    logger.info(f'以Ray Driver方式运行: {" ".join(cmd)}')
    logger.info(f'脚本路径: {script_path}')

    try:
        # 用 Popen 而不是 run：把 process 对象存到 task_info.process，让 /agent/task/stop 真的能 terminate
        # subprocess.run 内部会等结束，外部拿不到 Popen，stopJob 永远只能改 status 杀不掉进程
        proc = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            env=script_env,
            cwd='/tmp'
        )
        task_info.process = proc
        logger.info(f'Python 进程已启动: jobId={job_id}, pid={proc.pid}')

        try:
            stdout_bytes, stderr_bytes = proc.communicate(timeout=600)
        except subprocess.TimeoutExpired:
            proc.kill()
            stdout_bytes, stderr_bytes = proc.communicate()
            task_info.status = 'FAILED'
            task_info.error = '任务执行超时（>10 分钟），已强制 kill 进程'
            logger.error(f'任务 {job_id} 执行超时，已 kill pid={proc.pid}')
            return
        # communicate() 后 process 已结束，清空引用
        task_info.process = None
        result = subprocess.CompletedProcess(cmd, proc.returncode, stdout_bytes, stderr_bytes)

        if result.returncode == 0:
            task_info.status = 'SUCCEEDED'
            # 取 stdout 最后部分作为结果（避免过长）
            stdout_text = result.stdout.decode('utf-8', errors='replace') if result.stdout else ''
            task_info.result = stdout_text[-5000:] if stdout_text else '(无输出)'
            logger.info(f'任务 {job_id} 执行成功')
            logger.info(f'任务 {job_id} 输出尾部: {task_info.result[-200:]}')
        else:
            task_info.status = 'FAILED'
            # 完整记录 stdout 和 stderr，便于诊断
            stdout_text = result.stdout.decode('utf-8', errors='replace') if result.stdout else ''
            stderr_text = result.stderr.decode('utf-8', errors='replace') if result.stderr else ''
            stdout_tail = stdout_text[-2000:] if stdout_text else '(空)'
            stderr_tail = stderr_text[-2000:] if stderr_text else '(空)'
            task_info.error = (
                f'任务执行失败 [returncode={result.returncode}]\n'
                f'--- stdout (尾 2000 字符) ---\n{stdout_tail}\n'
                f'--- stderr (尾 2000 字符) ---\n{stderr_tail}'
            )
            logger.error(f'任务 {job_id} 执行失败: returncode={result.returncode}')
            logger.error(f'stderr: {stderr_tail}')
            logger.error(f'stdout: {stdout_tail}')

    except subprocess.TimeoutExpired:
        task_info.status = 'FAILED'
        task_info.error = '任务提交超时'
        logger.error(f'任务 {job_id} 提交超时')
    except Exception as e:
        task_info.status = 'FAILED'
        task_info.error = str(e)
        logger.error(f'任务 {job_id} 执行异常: {e}')
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
