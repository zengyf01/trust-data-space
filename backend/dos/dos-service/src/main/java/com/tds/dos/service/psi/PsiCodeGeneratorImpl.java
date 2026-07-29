package com.tds.dos.service.psi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PSI代码生成器实现
 * 根据任务参数生成SecretFlow Python代码
 */
@Component
public class PsiCodeGeneratorImpl implements IPsiCodeGenerator {

    @Value("${ray.address:ray://ray-head:10001}")
    private String rayAddress;

    @Value("${ray.party-a.address:127.0.0.1:50001}")
    private String partyAAddress;

    @Value("${ray.party-b.address:127.0.0.1:50002}")
    private String partyBAddress;

    @Override
    public String generatePsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                  String keyColumn, String protocol, String resultType, String role) {
        return generatePsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, protocol, resultType, role,
            rayAddress, partyAAddress, partyBAddress);
    }

    @Override
    public String generatePsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                  String keyColumn, String protocol, String resultType, String role,
                                  String rayAddr, String partyAAddr, String partyBAddr) {
        StringBuilder code = new StringBuilder();

        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# PSI任务执行脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        code.append("# 协议: ").append(protocol).append("\n");
        code.append("# 角色: ").append(role).append("\n");
        code.append("# Ray集群: ").append(rayAddr).append("\n");
        code.append("#\n\n");

        code.append("import os\n");
        code.append("import sys\n");
        code.append("import json\n");
        code.append("import secretflow as sf\n");
        code.append("from secretflow.device import SPU, HEU\n");
        code.append("from secretflow import reveal\n\n");

        // 构建 cluster config（包含 SPU）
        code.append("# SecretFlow 集群配置（包含SPU）\n");
        code.append("cluster_config = {\n");
        code.append("    'parties': {\n");
        code.append("        'alice': {\n");
        code.append("            'address': '").append(partyAAddr).append("'\n");
        code.append("        },\n");
        code.append("        'bob': {\n");
        code.append("            'address': '").append(partyBAddr).append("'\n");
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
        code.append("sf.init(address='").append(rayAddr).append("', cluster_config=cluster_config)\n\n");

        // 创建 SPU 设备
        code.append("# 创建SPU设备\n");
        code.append("spu = SPU(cluster_config['spu'])\n\n");

        // 协议选择
        code.append("# PSI参数配置\n");
        code.append("key_column = '").append(keyColumn).append("'\n");
        code.append("protocol = '").append(protocol).append("'\n");
        code.append("result_type = '").append(resultType).append("'\n");
        code.append("task_id = '").append(taskId).append("'\n\n");

        // 根据协议生成不同的代码
        if ("ECPSI".equalsIgnoreCase(protocol)) {
            code.append(generateEcPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType, role));
        } else if ("KKRTPSI".equalsIgnoreCase(protocol)) {
            code.append(generateKkrtPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType, role));
        } else if ("RR22PSI".equalsIgnoreCase(protocol)) {
            code.append(generateRr22PsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType, role));
        } else {
            code.append(generateDefaultPsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, resultType, role));
        }

        return code.toString();
    }

    @Override
    public String generateMultiPartyPsiCode(String taskId, Map<String, String> params) {
        String partyADataPath = params.getOrDefault("partyADataPath", "/tmp/party_a.csv");
        String partyBDataPath = params.getOrDefault("partyBDataPath", "/tmp/party_b.csv");
        String keyColumn = params.getOrDefault("keyColumn", "id");
        String protocol = params.getOrDefault("protocol", "ECPSI");
        String resultType = params.getOrDefault("resultType", "INTERSECTION");

        return generatePsiCode(taskId, partyADataPath, partyBDataPath, keyColumn, protocol, resultType, "A");
    }

    private String generateEcPsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                     String keyColumn, String resultType, String role) {
        StringBuilder code = new StringBuilder();
        code.append("# ECPSI协议求交\n");
        code.append("def run_psi():\n");
        code.append("    # 读取数据\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        party_a_path = '").append(partyADataPath).append("'\n");
        code.append("        df_a = sf.read_csv(party_a_path, delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        \n");
        code.append("        # 使用SPU执行PSI\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='ECPSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_a,\n");
        code.append("            party='alice'\n");
        code.append("        )\n");
        code.append("        \n");
        code.append("        print(f'PSI完成, 交集数量: {len(result)}')\n");
        code.append("        # 输出结果到文件\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_a.csv', index=False)\n");
        code.append("        print(f'结果已保存到 /tmp/psi_result_").append(taskId).append("_a.csv')\n");
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        party_b_path = '").append(partyBDataPath).append("'\n");
        code.append("        df_b = sf.read_csv(party_b_path, delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        \n");
        code.append("        # 使用SPU执行PSI\n");
        code.append("        result = sf.psi(\n");
        code.append("            key_column='").append(keyColumn).append("',\n");
        code.append("            protocol='ECPSI',\n");
        code.append("            spu=spu,\n");
        code.append("            input=df_b,\n");
        code.append("            party='bob'\n");
        code.append("        )\n");
        code.append("        \n");
        code.append("        print(f'PSI完成, 交集数量: {len(result)}')\n");
        code.append("        # 输出结果到文件\n");
        code.append("        result_df = result.to_dataframe()\n");
        code.append("        result_df.to_csv('/tmp/psi_result_").append(taskId).append("_b.csv', index=False)\n");
        code.append("        print(f'结果已保存到 /tmp/psi_result_").append(taskId).append("_b.csv')\n");
        code.append("    \n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }

    private String generateKkrtPsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                       String keyColumn, String resultType, String role) {
        StringBuilder code = new StringBuilder();
        code.append("# KKRTPSI协议求交\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        \n");
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
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        \n");
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
        code.append("    \n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }

    private String generateRr22PsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                       String keyColumn, String resultType, String role) {
        StringBuilder code = new StringBuilder();
        code.append("# RR22PSI协议求交 (Replicated Random Rotation PSI)\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        \n");
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
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        \n");
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
        code.append("    \n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }

    private String generateDefaultPsiCode(String taskId, String partyADataPath, String partyBDataPath,
                                          String keyColumn, String resultType, String role) {
        StringBuilder code = new StringBuilder();
        code.append("# 默认PSI协议求交 (ECPSI)\n");
        code.append("def run_psi():\n");
        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        \n");
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
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        \n");
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
        code.append("    \n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_psi()\n");
        return code.toString();
    }
}
