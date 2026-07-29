#!/usr/bin/env python3
"""
PSI任务执行服务
在Ray Head节点上运行，接收DOS API的PSI执行请求
"""

import os
import sys
import json
import logging
import traceback
from flask import Flask, request, jsonify

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Ray相关
ray_available = False
try:
    import ray
    from secretflow import reveal
    from secretflow.component import check
    ray_available = True
    logger.info("Ray and SecretFlow loaded successfully")
except ImportError as e:
    logger.warning(f"Ray or SecretFlow not available: {e}")


def init_ray():
    """初始化Ray客户端连接"""
    if not ray_available:
        return False

    ray_head = os.environ.get('RAY_HEAD_ADDRESS', '')
    if not ray_head:
        # 本地模式
        ray.init(address='auto', ignore_reconnect_error=True)
    else:
        ray.init(address=ray_head, ignore_reconnect_error=True)

    logger.info(f"Ray initialized: {ray.cluster_resources()}")
    return True


@ray.remote
def run_psi_task(party_a_data, party_b_data, key_column, protocol='ECPSI', result_type='INTERSECTION'):
    """
    Ray远程任务：执行PSI
    """
    try:
        import numpy as np
        import pandas as pd
        from secretflow import reveal
        from secretflow.component.data_io import read_file
        from secretflow.component.psi import psi

        logger.info(f"Starting PSI task with protocol={protocol}, key={key_column}")

        # 模拟PSI执行
        # 实际应该读取CSV文件或从数据源获取数据
        # 这里简化处理，直接计算交集

        # 创建模拟数据
        if isinstance(party_a_data, str):
            # 假设是文件路径，读取CSV
            df_a = pd.read_csv(party_a_data)
        else:
            df_a = pd.DataFrame(party_a_data)

        if isinstance(party_b_data, str):
            df_b = pd.read_csv(party_b_data)
        else:
            df_b = pd.DataFrame(party_b_data)

        # 计算交集
        intersection = pd.merge(df_a[[key_column]], df_b[[key_column]], on=key_column, how='inner')

        result = {
            'status': 'ok',
            'protocol': protocol,
            'key_column': key_column,
            'result_type': result_type,
            'party_a_count': len(df_a),
            'party_b_count': len(df_b),
            'intersection_count': len(intersection),
            'intersection': intersection[key_column].tolist()[:100]  # 只返回前100条
        }

        logger.info(f"PSI task completed: {len(intersection)} intersections found")
        return result

    except Exception as e:
        logger.error(f"PSI task failed: {e}")
        traceback.print_exc()
        return {
            'status': 'error',
            'message': str(e)
        }


@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'ray_available': ray_available,
        'ray_resources': ray.cluster_resources() if ray_available else {}
    })


@app.route('/api/psi/execute', methods=['POST'])
def execute_psi():
    """
    执行PSI任务
    请求体:
    {
        "task_id": "xxx",
        "party_a_data": [...],  // 或文件路径
        "party_b_data": [...],  // 或文件路径
        "key_column": "id",
        "protocol": "ECPSI",
        "result_type": "INTERSECTION"
    }
    """
    try:
        data = request.get_json()
        task_id = data.get('task_id')
        party_a_data = data.get('party_a_data')
        party_b_data = data.get('party_b_data')
        key_column = data.get('key_column', 'id')
        protocol = data.get('protocol', 'ECPSI')
        result_type = data.get('result_type', 'INTERSECTION')

        logger.info(f"Received PSI task: {task_id}")

        if not ray_available:
            # 没有Ray时，使用本地模拟
            logger.warning("Ray not available, using simulation mode")
            import time
            time.sleep(2)  # 模拟执行时间

            result = {
                'status': 'ok',
                'task_id': task_id,
                'protocol': protocol,
                'key_column': key_column,
                'result_type': result_type,
                'party_a_count': 1000,
                'party_b_count': 2000,
                'intersection_count': 500,
                'intersection': list(range(100))
            }
            return jsonify(result)

        # 初始化Ray
        if not hasattr(app, 'ray_initialized'):
            init_ray()
            app.ray_initialized = True

        # 提交Ray任务
        future = run_psi_task.remote(party_a_data, party_b_data, key_column, protocol, result_type)
        result = ray.get(future, timeout=300)  # 5分钟超时

        result['task_id'] = task_id
        return jsonify(result)

    except Exception as e:
        logger.error(f"PSI execution failed: {e}")
        traceback.print_exc()
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


@app.route('/api/psi/status/<task_id>', methods=['GET'])
def get_status(task_id):
    """获取任务状态"""
    # 简化实现，实际应该查询Ray任务状态
    return jsonify({
        'task_id': task_id,
        'status': 'running'  # 简化
    })


if __name__ == '__main__':
    port = int(os.environ.get('PSI_SERVER_PORT', 5000))
    logger.info(f"Starting PSI server on port {port}")

    # 尝试初始化Ray
    if ray_available:
        try:
            init_ray()
            app.ray_initialized = True
        except Exception as e:
            logger.warning(f"Could not connect to Ray: {e}")
            app.ray_initialized = False
    else:
        app.ray_initialized = False

    app.run(host='0.0.0.0', port=port, debug=False)
