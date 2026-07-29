package com.tds.dos.service.privacycompute.code;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MPC (Multi-Party Computation) 代码生成器
 * 生成SecretFlow MPC安全多方计算任务代码
 */
@Slf4j
@Component
public class MpcCodeGenerator implements ICodeGenerator {

    @Override
    public String getTaskType() {
        return "MPC";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        // 获取基础参数
        String partyADataPath = (String) params.getOrDefault("partyADataPath", "/tmp/party_a.csv");
        String partyBDataPath = (String) params.getOrDefault("partyBDataPath", "/tmp/party_b.csv");
        String resultType = (String) params.getOrDefault("resultType", "SUM");

        // 获取Ray集群地址
        String rayAddress = (String) params.getOrDefault("rayAddress", "ray://127.0.0.1:6379");
        // 获取参与方地址（Agent HTTP地址）
        String partyAAddress = (String) params.getOrDefault("partyAAddress", "127.0.0.1:8081");
        String partyBAddress = (String) params.getOrDefault("partyBAddress", "127.0.0.1:8081");

        // MPC特定参数
        String operation = (String) params.getOrDefault("operation", "SUM"); // SUM, AVG, MIN, MAX, etc.
        String[] inputColumns = params.get("inputColumns") != null
            ? ((String) params.get("inputColumns")).split(",")
            : new String[]{"value"};

        StringBuilder code = new StringBuilder();

        // 文件头注释
        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# MPC任务执行脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        code.append("# 操作类型: ").append(operation).append("\n");
        code.append("# Ray集群: ").append(rayAddress).append("\n");
        code.append("#\n\n");

        // 导入
        code.append("import os\n");
        code.append("import sys\n");
        code.append("import json\n");
        code.append("import secretflow as sf\n");
        code.append("from secretflow.device import SPU, HEU\n");
        code.append("from secretflow import reveal\n\n");

        // 构建 cluster config
        code.append("# SecretFlow 集群配置（包含SPU和HEU）\n");
        code.append("cluster_config = {\n");
        code.append("    'parties': {\n");
        code.append("        'alice': {\n");
        code.append("            'address': '").append(partyAAddress).append("'\n");
        code.append("        },\n");
        code.append("        'bob': {\n");
        code.append("            'address': '").append(partyBAddress).append("'\n");
        code.append("        }\n");
        code.append("    },\n");
        code.append("    'spu': {\n");
        code.append("        'kind': 'smc',\n");
        code.append("        'config': {\n");
        code.append("            'json_config': json.dumps({\n");
        code.append("                'protocol': 'semi2k',\n");
        code.append("                'field': 'goldilocks',\n");
        code.append("                'pre_limit_mode': 'FILE',\n");
        code.append("                'bitmap_size_limit': '64MB',\n");
        code.append("            })\n");
        code.append("        }\n");
        code.append("    },\n");
        code.append("    'heu': {\n");
        code.append("        'kind': 'pheu',\n");
        code.append("        'config': {\n");
        code.append("            'json_config': json.dumps({\n");
        code.append("                'protocol': 'paillier',\n");
        code.append("                'key_size': 2048,\n");
        code.append("            })\n");
        code.append("        }\n");
        code.append("    }\n");
        code.append("}\n\n");

        // 初始化SecretFlow
        code.append("# 初始化SecretFlow环境\n");
        code.append("sf.init(address='").append(rayAddress).append("', cluster_config=cluster_config)\n\n");

        // 创建SPU和HEU设备
        code.append("# 创建SPU和HEU设备\n");
        code.append("spu = SPU(cluster_config['spu'])\n");
        code.append("heu = HEU(cluster_config['heu'], 'alice')\n\n");

        // 生成MPC计算代码
        code.append(generateMpcCode(taskId, partyADataPath, partyBDataPath, operation, inputColumns));

        return code.toString();
    }

    @Override
    public String validateParams(Map<String, Object> params) {
        if (params == null) {
            return "参数不能为空";
        }
        String operation = (String) params.get("operation");
        if (operation == null || operation.isEmpty()) {
            return "operation不能为空";
        }
        return null;
    }

    private String generateMpcCode(String taskId, String partyADataPath, String partyBDataPath,
                                   String operation, String[] inputColumns) {
        StringBuilder code = new StringBuilder();
        code.append("# MPC安全计算\n");
        code.append("def run_mpc():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        # 将数据转换为HEU设备\n");
        code.append("        heu_a = HEU(cluster_config['heu'], 'alice')\n");
        code.append("        input_a = heu_a(df_a)\n");
        code.append("        \n");
        code.append("        # 执行MPC操作\n");
        code.append("        if '").append(operation).append("' == 'SUM':\n");
        code.append("            result = sf.sum(input_a)\n");
        code.append("        elif '").append(operation).append("' == 'AVG':\n");
        code.append("            result = sf.mean(input_a)\n");
        code.append("        elif '").append(operation).append("' == 'MIN':\n");
        code.append("            result = sf.min(input_a)\n");
        code.append("        elif '").append(operation).append("' == 'MAX':\n");
        code.append("            result = sf.max(input_a)\n");
        code.append("        else:\n");
        code.append("            raise ValueError(f'Unsupported operation: ").append(operation).append("')\n");
        code.append("        \n");
        code.append("        print(f'MPC计算完成')\n");
        code.append("        # 揭示结果（仅示例，实际应安全揭示）\n");
        code.append("        revealed_result = reveal(result)\n");
        code.append("        print(f'结果: {revealed_result}')\n");
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        # 将数据转换为HEU设备\n");
        code.append("        heu_b = HEU(cluster_config['heu'], 'bob')\n");
        code.append("        input_b = heu_b(df_b)\n");
        code.append("        \n");
        code.append("        # 执行MPC操作\n");
        code.append("        if '").append(operation).append("' == 'SUM':\n");
        code.append("            result = sf.sum(input_b)\n");
        code.append("        elif '").append(operation).append("' == 'AVG':\n");
        code.append("            result = sf.mean(input_b)\n");
        code.append("        elif '").append(operation).append("' == 'MIN':\n");
        code.append("            result = sf.min(input_b)\n");
        code.append("        elif '").append(operation).append("' == 'MAX':\n");
        code.append("            result = sf.max(input_b)\n");
        code.append("        else:\n");
        code.append("            raise ValueError(f'Unsupported operation: ").append(operation).append("')\n");
        code.append("        \n");
        code.append("        print(f'MPC计算完成')\n");
        code.append("        revealed_result = reveal(result)\n");
        code.append("        print(f'结果: {revealed_result}')\n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_mpc()\n");
        return code.toString();
    }
}
