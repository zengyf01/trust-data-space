package com.tds.dos.service.privacycompute.code;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FL (Federated Learning) 代码生成器
 * 生成SecretFlow联邦学习任务代码
 */
@Slf4j
@Component
public class FlCodeGenerator implements ICodeGenerator {

    @Override
    public String getTaskType() {
        return "FEDERATED_LEARNING";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        // 获取基础参数
        String partyADataPath = (String) params.getOrDefault("partyADataPath", "/tmp/party_a.csv");
        String partyBDataPath = (String) params.getOrDefault("partyBDataPath", "/tmp/party_b.csv");

        // 获取Ray集群地址
        String rayAddress = (String) params.getOrDefault("rayAddress", "ray://127.0.0.1:6379");
        // 获取参与方地址（Agent HTTP地址）
        String partyAAddress = (String) params.getOrDefault("partyAAddress", "127.0.0.1:8081");
        String partyBAddress = (String) params.getOrDefault("partyBAddress", "127.0.0.1:8081");

        // FL特定参数
        String modelType = (String) params.getOrDefault("modelType", "LR"); // LR, DNN, CNN, etc.
        int epochs = params.get("epochs") != null ? Integer.parseInt(params.get("epochs").toString()) : 10;
        float learningRate = params.get("learningRate") != null
            ? Float.parseFloat(params.get("learningRate").toString()) : 0.01f;
        String[] featureColumns = params.get("featureColumns") != null
            ? ((String) params.get("featureColumns")).split(",")
            : new String[]{"feature1", "feature2"};
        String labelColumn = (String) params.getOrDefault("labelColumn", "label");

        StringBuilder code = new StringBuilder();

        // 文件头注释
        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# 联邦学习任务执行脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(java.time.LocalDateTime.now()).append("\n");
        code.append("# 模型类型: ").append(modelType).append("\n");
        code.append("# Ray集群: ").append(rayAddress).append("\n");
        code.append("#\n\n");

        // 导入
        code.append("import os\n");
        code.append("import sys\n");
        code.append("import json\n");
        code.append("import secretflow as sf\n");
        code.append("from secretflow.device import SPU\n");
        code.append("from secretflow.ml.nn import FLModel\n");
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

        // 生成FL代码
        code.append(generateFlCode(taskId, partyADataPath, partyBDataPath, modelType, epochs,
            learningRate, featureColumns, labelColumn));

        return code.toString();
    }

    @Override
    public String validateParams(Map<String, Object> params) {
        if (params == null) {
            return "参数不能为空";
        }
        String modelType = (String) params.get("modelType");
        if (modelType == null || modelType.isEmpty()) {
            return "modelType不能为空";
        }
        return null;
    }

    private String generateFlCode(String taskId, String partyADataPath, String partyBDataPath,
                                   String modelType, int epochs, float learningRate,
                                   String[] featureColumns, String labelColumn) {
        StringBuilder code = new StringBuilder();
        code.append("# 联邦学习训练\n");
        code.append("def run_fl():\n");
        code.append("    # 特征列和标签列\n");
        code.append("    feature_columns = [").append(convertToListString(featureColumns)).append("]\n");
        code.append("    label_column = '").append(labelColumn).append("'\n\n");

        code.append("    if sf.get_party() == 'alice':\n");
        code.append("        df_a = sf.read_csv('").append(partyADataPath).append("', delimiter=',')\n");
        code.append("        print(f'Alice数据加载完成，行数: {len(df_a)}')\n");
        code.append("        # 准备数据\n");
        code.append("        x_a = df_a[feature_columns]\n");
        code.append("        y_a = df_a[label_column]\n");
        code.append("        print(f'Alice样本数: {len(x_a)}')\n");
        code.append("        \n");
        code.append("        # 创建本地模型（联邦学习使用明文模型）\n");
        code.append("        if '").append(modelType).append("' == 'LR':\n");
        code.append("            from sklearn.linear_model import LogisticRegression\n");
        code.append("            model = LogisticRegression()\n");
        code.append("        elif '").append(modelType).append("' == 'DNN':\n");
        code.append("            import torch\n");
        code.append("            model = torch.nn.Sequential(\n");
        code.append("                torch.nn.Linear(len(feature_columns), 64),\n");
        code.append("                torch.nn.ReLU(),\n");
        code.append("                torch.nn.Linear(64, 32),\n");
        code.append("                torch.nn.ReLU(),\n");
        code.append("                torch.nn.Linear(32, 2)\n");
        code.append("            )\n");
        code.append("        else:\n");
        code.append("            raise ValueError(f'Unsupported model type: ").append(modelType).append("')\n");
        code.append("        \n");
        code.append("        print(f'Alice本地模型创建完成')\n");
        code.append("        \n");
        code.append("    elif sf.get_party() == 'bob':\n");
        code.append("        df_b = sf.read_csv('").append(partyBDataPath).append("', delimiter=',')\n");
        code.append("        print(f'Bob数据加载完成，行数: {len(df_b)}')\n");
        code.append("        # 准备数据\n");
        code.append("        x_b = df_b[feature_columns]\n");
        code.append("        y_b = df_b[label_column]\n");
        code.append("        print(f'Bob样本数: {len(x_b)}')\n");
        code.append("        \n");
        code.append("        # 创建本地模型\n");
        code.append("        if '").append(modelType).append("' == 'LR':\n");
        code.append("            from sklearn.linear_model import LogisticRegression\n");
        code.append("            model = LogisticRegression()\n");
        code.append("        elif '").append(modelType).append("' == 'DNN':\n");
        code.append("            import torch\n");
        code.append("            model = torch.nn.Sequential(\n");
        code.append("                torch.nn.Linear(len(feature_columns), 64),\n");
        code.append("                torch.nn.ReLU(),\n");
        code.append("                torch.nn.Linear(64, 32),\n");
        code.append("                torch.nn.ReLU(),\n");
        code.append("                torch.nn.Linear(32, 2)\n");
        code.append("            )\n");
        code.append("        else:\n");
        code.append("            raise ValueError(f'Unsupported model type: ").append(modelType).append("')\n");
        code.append("        \n");
        code.append("        print(f'Bob本地模型创建完成')\n");
        code.append("        \n");
        code.append("    # 注意：实际的联邦学习需要使用FLModel进行模型聚合\n");
        code.append("    # 这里生成的是各方本地训练的示例代码\n");
        code.append("    print(f'联邦学习任务本地训练完成')\n");
        code.append("if __name__ == '__main__':\n");
        code.append("    run_fl()\n");
        return code.toString();
    }

    private String convertToListString(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("'").append(arr[i].trim()).append("'");
        }
        return sb.toString();
    }
}
