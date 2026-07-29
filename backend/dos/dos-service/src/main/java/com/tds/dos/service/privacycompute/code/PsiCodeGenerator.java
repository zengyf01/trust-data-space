package com.tds.dos.service.privacycompute.code;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PSI (Private Set Intersection) 代码生成器
 * 生成SecretFlow PSI求交任务代码
 */
@Slf4j
@Component
public class PsiCodeGenerator implements ICodeGenerator {

    @Override
    public String getTaskType() {
        return "PSI";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        String partyADataPath = (String) params.getOrDefault("partyADataPath", "/tmp/party_a.csv");
        String partyBDataPath = (String) params.getOrDefault("partyBDataPath", "/tmp/party_b.csv");
        String keyColumn = (String) params.getOrDefault("keyColumn", "id");
        String protocol = (String) params.getOrDefault("protocol", "ECPSI");
        String resultType = (String) params.getOrDefault("resultType", "INTERSECTION");

        // 获取Ray集群地址
        String rayAddress = (String) params.getOrDefault("rayAddress", "ray://127.0.0.1:6379");
        // 获取参与方地址（Agent HTTP地址）
        String partyAAddress = (String) params.getOrDefault("partyAAddress", "127.0.0.1:8081");
        String partyBAddress = (String) params.getOrDefault("partyBAddress", "127.0.0.1:8081");

        StringBuilder code = new StringBuilder();

        // 文件头注释
        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# PSI任务执行脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        code.append("# 协议: ").append(protocol).append("\n");
        code.append("# Ray集群: ").append(rayAddress).append("\n");
        code.append("#\n\n");

        // 导入
        code.append("import os\n");
        code.append("import sys\n");
        code.append("import json\n");
        code.append("import secretflow as sf\n");
        code.append("from secretflow.device import SPU\n");
        code.append("from secretflow import reveal\n\n");

        // 构建 cluster config
        code.append("# SecretFlow 集群配置（包含SPU）\n");
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
        code.append("                'mmcs_hmac_interaction_limit': '22',\n");
        code.append("                'mialect_interaction_limit': '7',\n");
        code.append("            })\n");
        code.append("        }\n");
        code.append("    }\n");
        code.append("}\n\n");

        // 初始化SecretFlow
        code.append("# 初始化SecretFlow环境\n");
        code.append("sf.init(address='").append(rayAddress).append("', cluster_config=cluster_config)\n\n");

        // 创建SPU设备
        code.append("# 创建SPU设备\n");
        code.append("spu = SPU(cluster_config['spu'])\n\n");

        // 根据协议生成代码
        if ("ECPSI".equalsIgnoreCase(protocol)) {
            code.append(generateEcPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType));
        } else if ("KKRTPSI".equalsIgnoreCase(protocol)) {
            code.append(generateKkrtPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType));
        } else if ("RR22PSI".equalsIgnoreCase(protocol)) {
            code.append(generateRr22PsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType));
        } else {
            code.append(generateEcPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType));
        }

        return code.toString();
    }

    @Override
    public String validateParams(Map<String, Object> params) {
        if (params == null) {
            return "参数不能为空";
        }
        String keyColumn = (String) params.get("keyColumn");
        if (keyColumn == null || keyColumn.isEmpty()) {
            return "keyColumn不能为空";
        }
        return null;
    }

    private String generateEcPsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                     String keyColumn, String resultType) {
        StringBuilder code = new StringBuilder();
        code.append("# ECPSI协议求交\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='ECPSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_a,\n");
        code.append("            party='alice'\n");
        code.append("        )\n");
        code.append("        print(f'PSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_a.csv', index=False)\n");
        code.append("        print(f'结果已保存到 /tmp/psi_result_").append(taskId).append("_a.csv')\n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='ECPSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_b,\n");
        code.append("            party='bob'\n");
        code.append("        )\n");
        code.append("        print(f'PSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_b.csv', index=False)\n");
        code.append("        print(f'结果已保存到 /tmp/psi_result_").append(taskId).append("_b.csv')\n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }

    private String generateKkrtPsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                       String keyColumn, String resultType) {
        StringBuilder code = new StringBuilder();
        code.append("# KKRTPSI协议求交\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='KKRT_PSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_a,\n");
        code.append("            party='alice'\n");
        code.append("        )\n");
        code.append("        print(f'KKRTPSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_a.csv', index=False)\n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='KKRT_PSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_b,\n");
        code.append("            party='bob'\n");
        code.append("        )\n");
        code.append("        print(f'KKRTPSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_b.csv', index=False)\n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }

    private String generateRr22PsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                       String keyColumn, String resultType) {
        StringBuilder code = new StringBuilder();
        code.append("# RR22PSI协议求交\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='RR22_PSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_a,\n");
        code.append("            party='alice'\n");
        code.append("        )\n");
        code.append("        print(f'RR22PSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_a.csv', index=False)\n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='RR22_PSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_b,\n");
        code.append("            party='bob'\n");
        code.append("        )\n");
        code.append("        print(f'RR22PSI完成, 交集数量: {len(result)}')\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_b.csv', index=False)\n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }
}
