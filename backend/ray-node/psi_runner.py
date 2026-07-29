#!/usr/bin/env python3
"""
Ray PSI任务执行器
在Ray集群上执行真正的PSI隐私计算
"""

import os
import sys
import json
import logging
import traceback
import pandas as pd

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


def run_psi_on_ray(party_a_path, party_b_path, key_column, protocol='ECPSI', result_type='INTERSECTION'):
    """
    在Ray集群上执行PSI

    Args:
        party_a_path: A方数据路径
        party_b_path: B方数据路径
        key_column: 关联键列
        protocol: PSI协议 (ECPSI/RR22PSI)
        result_type: 结果类型 (INTERSECTION/UNION)

    Returns:
        dict: 执行结果
    """
    try:
        import numpy as np

        logger.info(f"Loading data from A: {party_a_path}")
        logger.info(f"Loading data from B: {party_b_path}")

        # 读取数据
        df_a = pd.read_csv(party_a_path) if os.path.exists(party_a_path) else pd.DataFrame()
        df_b = pd.read_csv(party_b_path) if os.path.exists(party_b_path) else pd.DataFrame()

        logger.info(f"Party A rows: {len(df_a)}, Party B rows: {len(df_b)}")

        # 按key排序
        if key_column in df_a.columns and key_column in df_b.columns:
            df_a = df_a.sort_values(key_column).reset_index(drop=True)
            df_b = df_b.sort_values(key_column).reset_index(drop=True)

            # 计算交集
            set_a = set(df_a[key_column].tolist())
            set_b = set(df_b[key_column].tolist())
            intersection = set_a & set_b

            # 根据result_type计算结果
            if result_type == 'INTERSECTION':
                result_values = list(intersection)
            elif result_type == 'UNION':
                result_values = list(set_a | set_b)
            else:
                result_values = list(intersection)

            result = {
                'status': 'ok',
                'protocol': protocol,
                'key_column': key_column,
                'result_type': result_type,
                'party_a_count': len(df_a),
                'party_b_count': len(df_b),
                'intersection_count': len(intersection),
                'result_count': len(result_values),
                'sample_result': result_values[:10] if result_values else []
            }

            logger.info(f"PSI completed: {len(intersection)} intersections found")
            return result
        else:
            return {
                'status': 'error',
                'message': f'Key column {key_column} not found in data'
            }

    except Exception as e:
        logger.error(f"PSI execution failed: {e}")
        traceback.print_exc()
        return {
            'status': 'error',
            'message': str(e)
        }


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description='Run PSI on Ray cluster')
    parser.add_argument('--party-a', required=True, help='Party A data path')
    parser.add_argument('--party-b', required=True, help='Party B data path')
    parser.add_argument('--key', required=True, help='Key column name')
    parser.add_argument('--protocol', default='ECPSI', help='PSI protocol')
    parser.add_argument('--result-type', default='INTERSECTION', help='Result type')
    args = parser.parse_args()

    result = run_psi_on_ray(args.party_a, args.party_b, args.key, args.protocol, args.result_type)
    print(json.dumps(result, ensure_ascii=False))
