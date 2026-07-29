#!/usr/bin/env python3
"""
PSI任务执行服务
在Ray Head节点上运行，接收DOS API的PSI执行请求
包含详细执行日志和轨迹跟踪
"""

import os
import sys
import json
import logging
import traceback
import time
from datetime import datetime
from flask import Flask, request, jsonify
import pandas as pd

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(name)s: %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('/tmp/psi_execution.log', mode='a')
    ]
)
logger = logging.getLogger('psi_service')

app = Flask(__name__)

# Ray相关
ray_available = False
try:
    import ray
    ray_available = True
    logger.info("Ray loaded successfully")
except ImportError as e:
    logger.warning(f"Ray not available: {e}")


def add_trace(trace_list, step, status, message, details=None):
    """添加执行轨迹步骤到列表"""
    trace_entry = {
        'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3],
        'step': step,
        'status': status,
        'message': message,
        'details': details
    }
    trace_list.append(trace_entry)
    logger.info(f"[step {step}] {status}: {message}")


def init_ray():
    """初始化Ray客户端连接"""
    if not ray_available:
        return False
    try:
        ray_head = os.environ.get('RAY_HEAD_ADDRESS', '')
        if not ray_head:
            ray.init(address='auto')
        else:
            ray.init(address=ray_head)
        logger.info(f"Ray initialized: {ray.cluster_resources()}")
        return True
    except Exception as e:
        logger.error(f"Ray init failed: {e}")
        return False


@ray.remote
def run_psi_task(task_id, party_a_path, party_b_path, key_column, protocol, result_type):
    """
    Ray远程任务：执行PSI
    包含详细日志输出
    """
    trace_list = []

    try:
        # Step 1: 初始化任务
        add_trace(trace_list, 1, 'ok', f'Ray任务初始化完成')

        # Step 2: 加载A方数据
        add_trace(trace_list, 2, 'running', f'开始加载A方数据: {party_a_path}')
        if os.path.exists(party_a_path):
            df_a = pd.read_csv(party_a_path)
            add_trace(trace_list, 2, 'ok', f'A方数据加载完成', {
                'path': party_a_path,
                'rows': len(df_a),
                'columns': list(df_a.columns)
            })
            logger.info(f"[{task_id}] A方数据: {len(df_a)} rows")
        else:
            add_trace(trace_list, 2, 'error', f'A方数据文件不存在: {party_a_path}')
            return {'status': 'error', 'message': f'A方数据文件不存在: {party_a_path}', 'execution_trace': trace_list}

        # Step 3: 加载B方数据
        add_trace(trace_list, 3, 'running', f'开始加载B方数据: {party_b_path}')
        if os.path.exists(party_b_path):
            df_b = pd.read_csv(party_b_path)
            add_trace(trace_list, 3, 'ok', f'B方数据加载完成', {
                'path': party_b_path,
                'rows': len(df_b),
                'columns': list(df_b.columns)
            })
            logger.info(f"[{task_id}] B方数据: {len(df_b)} rows")
        else:
            add_trace(trace_list, 3, 'error', f'B方数据文件不存在: {party_b_path}')
            return {'status': 'error', 'message': f'B方数据文件不存在: {party_b_path}', 'execution_trace': trace_list}

        # Step 4: 数据预处理
        add_trace(trace_list, 4, 'running', '开始数据预处理（排序、去重）')
        df_a_sorted = df_a.sort_values(key_column).reset_index(drop=True)
        df_b_sorted = df_b.sort_values(key_column).reset_index(drop=True)
        add_trace(trace_list, 4, 'ok', '数据预处理完成', {
            'a_rows_after_dedup': len(df_a_sorted),
            'b_rows_after_dedup': len(df_b_sorted)
        })

        # Step 5: PSI计算
        add_trace(trace_list, 5, 'running', f'开始PSI计算，协议: {protocol}, 类型: {result_type}')

        set_a = set(df_a_sorted[key_column].tolist())
        set_b = set(df_b_sorted[key_column].tolist())

        if result_type == 'UNION':
            result_set = set_a | set_b
        else:
            result_set = set_a & set_b

        result_list = sorted(list(result_set))
        add_trace(trace_list, 5, 'ok', f'PSI计算完成', {
            'protocol': protocol,
            'result_type': result_type,
            'a_unique_count': len(set_a),
            'b_unique_count': len(set_b),
            'result_count': len(result_list)
        })

        # Step 6: 返回结果
        result = {
            'status': 'ok',
            'task_id': task_id,
            'protocol': protocol,
            'key_column': key_column,
            'result_type': result_type,
            'party_a_count': len(df_a),
            'party_b_count': len(df_b),
            'intersection_count': len(result_list),
            'result_count': len(result_list),
            'sample_result': result_list[:20],
            'execution_trace': trace_list
        }

        add_trace(trace_list, 6, 'ok', '任务执行完成')
        logger.info(f"[{task_id}] PSI执行成功: {len(result_list)} intersections")
        return result

    except Exception as e:
        error_msg = str(e)
        logger.error(f"[{task_id}] PSI执行失败: {error_msg}")
        add_trace(trace_list, 99, 'error', f'执行异常: {error_msg}')
        return {
            'status': 'error',
            'task_id': task_id,
            'message': error_msg,
            'execution_trace': trace_list
        }


@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'ray_available': ray_available
    })


@app.route('/api/psi/execute', methods=['POST'])
def execute_psi():
    """
    执行PSI任务
    """
    task_id = None
    try:
        data = request.get_json()
        task_id = data.get('task_id', 'unknown')
        party_a_path = data.get('party_a_data_path')
        party_b_path = data.get('party_b_data_path')
        key_column = data.get('key_column', 'id')
        protocol = data.get('protocol', 'ECPSI')
        result_type = data.get('result_type', 'INTERSECTION')

        logger.info(f"{'='*60}")
        logger.info(f"[{task_id}] PSI任务开始执行")
        logger.info(f"[{task_id}] A方数据: {party_a_path}")
        logger.info(f"[{task_id}] B方数据: {party_b_path}")
        logger.info(f"[{task_id}] 关联键: {key_column}")
        logger.info(f"{'='*60}")

        if not ray_available:
            return jsonify({
                'status': 'error',
                'task_id': task_id,
                'message': 'Ray not available'
            }), 500

        # 初始化Ray
        if not hasattr(app, 'ray_initialized'):
            if not init_ray():
                return jsonify({
                    'status': 'error',
                    'task_id': task_id,
                    'message': 'Failed to connect to Ray'
                }), 500
            app.ray_initialized = True

        logger.info(f"[{task_id}] 任务已提交到Ray集群")

        # 提交任务
        future = run_psi_task.remote(
            task_id, party_a_path, party_b_path,
            key_column, protocol, result_type
        )

        # 等待结果（超时5分钟）
        result = ray.get(future, timeout=300)

        logger.info(f"[{task_id}] PSI任务执行完成: {result.get('status')}")
        return jsonify(result)

    except Exception as e:
        error_msg = str(e)
        logger.error(f"[{task_id}] PSI执行异常: {error_msg}")
        return jsonify({
            'status': 'error',
            'task_id': task_id,
            'message': error_msg
        }), 500


if __name__ == '__main__':
    port = int(os.environ.get('PSI_SERVER_PORT', 5000))
    logger.info(f"Starting PSI server on port {port}")

    # 尝试初始化Ray
    if ray_available:
        try:
            if init_ray():
                app.ray_initialized = True
                logger.info("Ray connection established")
        except Exception as e:
            logger.warning(f"Could not connect to Ray: {e}")
            app.ray_initialized = False
    else:
        app.ray_initialized = False

    app.run(host='0.0.0.0', port=port, debug=False, threaded=True)
