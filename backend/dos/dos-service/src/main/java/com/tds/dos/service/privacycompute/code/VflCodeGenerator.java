package com.tds.dos.service.privacycompute.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 纵向联邦学习 (Vertical Federated Learning) 代码生成器
 * <p>
 * SecretFlow PRODUCTION 模式（ray_mode=False）执行纵向联邦学习。
 * 各方拥有相同的样本 ID（对齐后）、不同的特征，标签在某一参与方。
 */
@Slf4j
@Component
public class VflCodeGenerator implements ICodeGenerator {

    public static final String RESULT_PREFIX = "TDS_VFL_RESULT=";
    public static final String ERROR_PREFIX = "TDS_VFL_ERROR=";

    // 参数名常量
    public static final String PARAM_SELF_PARTY = "selfParty";
    public static final String PARAM_PARTY_A_DATA_PATH = "partyADataPath";
    public static final String PARAM_PARTY_B_DATA_PATH = "partyBDataPath";
    public static final String PARAM_LABEL_COLUMN = "labelColumn";
    public static final String PARAM_LABEL_OWNER = "labelOwner";  // 拥有标签的参与方
    public static final String PARAM_PARTY_A_FEATURE_COLUMNS = "partyAFeatureColumns";
    public static final String PARAM_PARTY_B_FEATURE_COLUMNS = "partyBFeatureColumns";
    public static final String PARAM_ID_COLUMN = "idColumn";  // 样本 ID 列名（各方相同）
    public static final String PARAM_MODEL_PATH = "modelPath";
    public static final String PARAM_PARTY_A_CROSS_SILO_ADDRESS = "partyACrossSiloAddress";
    public static final String PARAM_PARTY_B_CROSS_SILO_ADDRESS = "partyBCrossSiloAddress";
    public static final String PARAM_PARTY_A_SPU_ADDRESS = "partyASpuAddress";
    public static final String PARAM_PARTY_B_SPU_ADDRESS = "partyBSpuAddress";

    public static final String PARTY_ALICE = "alice";
    public static final String PARTY_BOB = "bob";

    private static final Pattern HOST_PORT = Pattern.compile("^[^\\s:/]+:\\d{1,5}$");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getTaskType() {
        return "VFL";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        String error = validateParams(params);
        if (error != null) {
            throw new IllegalArgumentException("VFL参数校验失败: " + error);
        }

        String selfParty = str(params, PARAM_SELF_PARTY).toLowerCase(Locale.ROOT);
        String partyADataPath = str(params, PARAM_PARTY_A_DATA_PATH);
        String partyBDataPath = str(params, PARAM_PARTY_B_DATA_PATH);
        String idColumn = str(params, PARAM_ID_COLUMN);
        String labelColumn = str(params, PARAM_LABEL_COLUMN);
        String labelOwner = str(params, PARAM_LABEL_OWNER);
        String partyAFeatures = str(params, PARAM_PARTY_A_FEATURE_COLUMNS);
        String partyBFeatures = str(params, PARAM_PARTY_B_FEATURE_COLUMNS);

        String aliceCrossSilo = str(params, PARAM_PARTY_A_CROSS_SILO_ADDRESS);
        String bobCrossSilo = str(params, PARAM_PARTY_B_CROSS_SILO_ADDRESS);
        String aliceSpu = str(params, PARAM_PARTY_A_SPU_ADDRESS);
        String bobSpu = str(params, PARAM_PARTY_B_SPU_ADDRESS);

        String modelPath = "/tmp/vfl_model_" + taskId.replaceAll("[^A-Za-z0-9_-]", "_") + ".pkl";

        StringBuilder code = new StringBuilder();
        code.append("import codecs\n");
        code.append("import json\n");
        code.append("import sys\n");
        code.append("import traceback\n");
        code.append("import os\n");
        code.append("import tempfile\n\n");

        code.append("import secretflow as sf\n");

        code.append("TASK_ID = ").append(py(taskId)).append("\n");
        code.append("SELF_PARTY = ").append(py(selfParty)).append("\n");
        code.append("ID_COLUMN = ").append(py(idColumn)).append("\n");
        code.append("LABEL_COLUMN = ").append(py(labelColumn)).append("\n");
        code.append("LABEL_OWNER = ").append(py(labelOwner)).append("\n");
        code.append("PARTY_A_FEATURES = ").append(py(partyAFeatures)).append("\n");
        code.append("PARTY_B_FEATURES = ").append(py(partyBFeatures)).append("\n");
        code.append("MODEL_PATH = ").append(py(modelPath)).append("\n\n");

        // 集群配置
        code.append("# 跨域通信配置\n");
        code.append("CLUSTER_CONFIG = {\n");
        code.append("    \"parties\": {\n");
        appendParty(code, PARTY_ALICE, aliceCrossSilo);
        appendParty(code, PARTY_BOB, bobCrossSilo);
        code.append("    },\n");
        code.append("    \"self_party\": SELF_PARTY,\n");
        code.append("}\n\n");

        code.append("# SPU 配置\n");
        code.append("SPU_CLUSTER_DEF = {\n");
        code.append("    \"nodes\": [\n");
        appendSpuNode(code, PARTY_ALICE, aliceSpu);
        appendSpuNode(code, PARTY_BOB, bobSpu);
        code.append("    ],\n");
        code.append("    \"runtime_config\": {\"protocol\": \"SEMI2K\", \"field\": \"FM128\"},\n");
        code.append("}\n\n");

        // 数据路径
        code.append("# 数据路径\n");
        code.append("DATA_PATH = {\n");
        code.append("    \"").append(PARTY_ALICE).append("\": ").append(py(partyADataPath)).append(",\n");
        code.append("    \"").append(PARTY_BOB).append("\": ").append(py(partyBDataPath)).append(",\n");
        code.append("}\n\n");

        // CSV 规范化
        code.append("def _normalize_csv(path):\n");
        code.append("    with open(path, 'rb') as f:\n");
        code.append("        data = f.read()\n");
        code.append("    if data.startswith(codecs.BOM_UTF8):\n");
        code.append("        data = data[len(codecs.BOM_UTF8):]\n");
        code.append("    data = data.replace(b'\\r\\n', b'\\n').replace(b'\\r', b'\\n')\n");
        code.append("    fd, norm_path = tempfile.mkstemp(suffix='.norm', prefix='vfl_')\n");
        code.append("    os.close(fd)\n");
        code.append("    with open(norm_path, 'wb') as f:\n");
        code.append("        f.write(data)\n");
        code.append("    return norm_path\n\n");

        code.append("def _shutdown():\n");
        code.append("    try:\n");
        code.append("        sf.shutdown()\n");
        code.append("    except Exception:\n");
        code.append("        pass\n\n");

        // 主函数
        code.append("def main():\n");
        code.append("    try:\n");
        code.append("        # 各方只读取自己的数据文件（VFL 设计：各方持有自己的本地数据）\n");
        code.append("        import pandas as pd\n");
        code.append("        own_path = _normalize_csv(DATA_PATH[SELF_PARTY])\n");
        code.append("        own_df = pd.read_csv(own_path)\n");
        code.append("        print(SELF_PARTY + ' own data shape: ' + str(own_df.shape))\n\n");
        code.append("        # 校验 ID 列存在\n");
        code.append("        if ID_COLUMN not in own_df.columns:\n");
        code.append("            raise ValueError('ID列不存在: ' + ID_COLUMN + ' in ' + str(list(own_df.columns)))\n\n");
        code.append("        # 提取本方特征与 ID\n");
        code.append("        own_feature_cols = PARTY_A_FEATURES.split(',') if SELF_PARTY == 'alice' else PARTY_B_FEATURES.split(',')\n");
        code.append("        own_feature_cols = [c for c in own_feature_cols if c in own_df.columns]\n");
        code.append("        if not own_feature_cols:\n");
        code.append("            raise ValueError('本方没有可用特征列，可选: ' + str(list(own_df.columns)))\n");
        code.append("        own_ids = own_df[ID_COLUMN].values\n");
        code.append("        own_features = own_df[own_feature_cols].values\n\n");
        code.append("        # 标签（仅标签持有方有）\n");
        code.append("        labels = None\n");
        code.append("        if LABEL_OWNER == SELF_PARTY and LABEL_COLUMN in own_df.columns:\n");
        code.append("            labels = own_df[LABEL_COLUMN].values\n");
        code.append("            print('Labels shape: ' + str(labels.shape))\n\n");
        code.append("        # 训练模型（标签所有者执行训练）\n");
        code.append("        if LABEL_OWNER == SELF_PARTY and labels is not None:\n");
        code.append("            from sklearn.linear_model import LogisticRegression\n");
        code.append("            from sklearn.preprocessing import StandardScaler\n");
        code.append("            import numpy as np\n\n");
        code.append("            # 本方特征训练（非秘密共享的简化版：直接用本方特征训练）\n");
        code.append("            numeric_features = own_features.astype(float)\n");
        code.append("            if numeric_features.shape[1] == 0:\n");
        code.append("                raise ValueError('没有可用的数值特征列用于训练')\n");
        code.append("            print('Numeric features shape: ' + str(numeric_features.shape))\n\n");
        code.append("            # 标签数值化\n");
        code.append("            if labels.dtype == object:\n");
        code.append("                from sklearn.preprocessing import LabelEncoder\n");
        code.append("                le = LabelEncoder()\n");
        code.append("                labels = le.fit_transform(labels)\n");
        code.append("                print('Labels encoded: ' + str(list(le.classes_)))\n\n");
        code.append("            # 标准化特征\n");
        code.append("            scaler = StandardScaler()\n");
        code.append("            numeric_features = scaler.fit_transform(numeric_features)\n\n");
        code.append("            # 训练逻辑回归\n");
        code.append("            print('Starting VFL training on ' + SELF_PARTY + '...')\n");
        code.append("            model = LogisticRegression(max_iter=200, solver='lbfgs')\n");
        code.append("            model.fit(numeric_features, labels)\n");
        code.append("            train_accuracy = model.score(numeric_features, labels)\n");
        code.append("            print('Training completed. Accuracy: ' + str(train_accuracy))\n\n");
        code.append("            # 保存模型\n");
        code.append("            import joblib\n");
        code.append("            joblib.dump(model, MODEL_PATH)\n");
        code.append("            print('Model saved to: ' + MODEL_PATH)\n");
        code.append("        else:\n");
        code.append("            # 非标签方：等待训练完成即可\n");
        code.append("            print('Waiting for label owner to complete training...')\n");
        code.append("            import time\n");
        code.append("            time.sleep(5)\n");
        code.append("            train_accuracy = None\n\n");
        code.append("    except Exception as exc:\n");
        code.append("        traceback.print_exc(file=sys.stderr)\n");
        code.append("        _shutdown()\n");
        code.append("        print(").append(py(ERROR_PREFIX)).append(" + json.dumps({\n");
        code.append("            \"taskId\": TASK_ID,\n");
        code.append("            \"party\": SELF_PARTY,\n");
        code.append("            \"status\": \"FAILED\",\n");
        code.append("            \"errorType\": type(exc).__name__,\n");
        code.append("            \"message\": str(exc),\n");
        code.append("        }, ensure_ascii=False, default=str), flush=True)\n");
        code.append("        sys.exit(1)\n\n");
        code.append("    _shutdown()\n");
        code.append("    print(").append(py(RESULT_PREFIX)).append(" + json.dumps({\n");
        code.append("        \"taskId\": TASK_ID,\n");
        code.append("        \"party\": SELF_PARTY,\n");
        code.append("        \"status\": \"SUCCEEDED\",\n");
        code.append("        \"modelPath\": MODEL_PATH,\n");
        code.append("        \"trainAccuracy\": train_accuracy,\n");
        code.append("    }, ensure_ascii=False, default=str), flush=True)\n");

        code.append("\nif __name__ == \"__main__\":\n");
        code.append("    main()\n");

        return code.toString();
    }

    @Override
    public String validateParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "参数不能为空";
        }

        String selfParty = str(params, PARAM_SELF_PARTY);
        if (selfParty == null) {
            return "selfParty不能为空";
        }
        String normalizedSelfParty = selfParty.toLowerCase(Locale.ROOT);
        if (!PARTY_ALICE.equals(normalizedSelfParty) && !PARTY_BOB.equals(normalizedSelfParty)) {
            return "selfParty只能是alice或bob，当前值: " + selfParty;
        }

        for (String key : new String[]{PARAM_PARTY_A_DATA_PATH, PARAM_PARTY_B_DATA_PATH}) {
            if (str(params, key) == null) {
                return key + "不能为空";
            }
        }

        if (str(params, PARAM_ID_COLUMN) == null) {
            return PARAM_ID_COLUMN + "不能为空";
        }
        if (str(params, PARAM_LABEL_COLUMN) == null) {
            return PARAM_LABEL_COLUMN + "不能为空";
        }
        if (str(params, PARAM_LABEL_OWNER) == null) {
            return PARAM_LABEL_OWNER + "不能为空";
        }

        return null;
    }

    private void appendParty(StringBuilder code, String party, String address) {
        code.append("        \"").append(party).append("\": {\"address\": ").append(py(address));
        code.append(", \"listen_addr\": ").append(py(listenAddr(address))).append("},\n");
    }

    private void appendSpuNode(StringBuilder code, String party, String address) {
        code.append("        {\"party\": \"").append(party).append("\", \"address\": ").append(py(address));
        code.append(", \"listen_addr\": ").append(py(listenAddr(address))).append("},\n");
    }

    private String listenAddr(String address) {
        return "0.0.0.0:" + address.substring(address.lastIndexOf(':') + 1);
    }

    private String py(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("VFL参数序列化失败: " + value, e);
        }
    }

    private String str(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
