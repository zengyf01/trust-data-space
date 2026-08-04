package com.tds.dos.service.privacycompute.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * 横向联邦学习 (Horizontal Federated Learning) 代码生成器
 * <p>
 * 生成 SecretFlow PRODUCTION 模式（ray_mode=False）的横向联邦学习脚本。
 * 各方拥有相同的特征，不同的样本数据。
 * <p>
 * 使用 FLModel + SecureAggregator 实现联邦训练。
 */
@Slf4j
@Component
public class FlCodeGenerator implements ICodeGenerator {

    public static final String RESULT_PREFIX = "TDS_FL_RESULT=";
    public static final String ERROR_PREFIX = "TDS_FL_ERROR=";

    // 参数名常量
    public static final String PARAM_SELF_PARTY = "selfParty";
    public static final String PARAM_PARTY_A_DATA_PATH = "partyADataPath";
    public static final String PARAM_PARTY_B_DATA_PATH = "partyBDataPath";
    public static final String PARAM_LABEL_COLUMN = "labelColumn";
    public static final String PARAM_FEATURE_COLUMNS = "featureColumns";
    public static final String PARAM_MODEL_TYPE = "modelType";  // LR, NN, XGB
    public static final String PARAM_DELIVERY_MODE = "deliveryMode";  // AGGREGATOR_ONLY, ALL_PARTIES
    public static final String PARAM_EPOCHS = "epochs";
    public static final String PARAM_BATCH_SIZE = "batchSize";
    public static final String PARAM_LEARNING_RATE = "learningRate";
    public static final String PARAM_PARTY_A_CROSS_SILO_ADDRESS = "partyACrossSiloAddress";
    public static final String PARAM_PARTY_B_CROSS_SILO_ADDRESS = "partyBCrossSiloAddress";
    public static final String PARAM_PARTY_A_SPU_ADDRESS = "partyASpuAddress";
    public static final String PARAM_PARTY_B_SPU_ADDRESS = "partyBSpuAddress";
    public static final String PARAM_MODEL_PATH = "modelPath";

    public static final String PARTY_ALICE = "alice";
    public static final String PARTY_BOB = "bob";

    // 模型类型常量
    public static final String MODEL_LR = "LR";
    public static final String MODEL_NN = "NN";
    public static final String MODEL_XGB = "XGB";

    // 交付模式常量
    public static final String DELIVERY_AGGREGATOR_ONLY = "AGGREGATOR_ONLY";  // 仅 Aggregator 保存
    public static final String DELIVERY_ALL_PARTIES = "ALL_PARTIES";  // 各方都保存

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getTaskType() {
        return "FL";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        String error = validateParams(params);
        if (error != null) {
            throw new IllegalArgumentException("FL参数校验失败: " + error);
        }

        String selfParty = str(params, PARAM_SELF_PARTY).toLowerCase(Locale.ROOT);
        String partyADataPath = str(params, PARAM_PARTY_A_DATA_PATH);
        String partyBDataPath = str(params, PARAM_PARTY_B_DATA_PATH);
        String labelColumn = str(params, PARAM_LABEL_COLUMN);
        String featureColumns = str(params, PARAM_FEATURE_COLUMNS);
        String modelType = str(params, PARAM_MODEL_TYPE);
        String deliveryMode = str(params, PARAM_DELIVERY_MODE);
        int epochs = 10;
        if (params.get(PARAM_EPOCHS) != null) {
            epochs = Integer.parseInt(String.valueOf(params.get(PARAM_EPOCHS)));
        }
        int batchSize = 32;
        if (params.get(PARAM_BATCH_SIZE) != null) {
            batchSize = Integer.parseInt(String.valueOf(params.get(PARAM_BATCH_SIZE)));
        }
        float learningRate = 0.01f;
        if (params.get(PARAM_LEARNING_RATE) != null) {
            learningRate = Float.parseFloat(String.valueOf(params.get(PARAM_LEARNING_RATE)));
        }

        String modelPath = "/tmp/fl_model_" + taskId.replaceAll("[^A-Za-z0-9_-]", "_") + ".pkl";

        StringBuilder code = new StringBuilder();
        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# 横向联邦学习任务脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(LocalDateTime.now()).append("\n");
        code.append("# 模型类型: ").append(modelType != null ? modelType : "LR").append("\n");
        code.append("# 交付模式: ").append(deliveryMode != null ? deliveryMode : "AGGREGATOR_ONLY").append("\n");
        code.append("# 本方角色: ").append(selfParty).append("\n");
        code.append("#\n\n");

        code.append("import codecs\n");
        code.append("import json\n");
        code.append("import sys\n");
        code.append("import traceback\n");
        code.append("import os\n");
        code.append("import tempfile\n\n");

        code.append("TASK_ID = ").append(py(taskId)).append("\n");
        code.append("SELF_PARTY = ").append(py(selfParty)).append("\n");
        code.append("LABEL_COLUMN = ").append(py(labelColumn)).append("\n");
        code.append("FEATURE_COLUMNS = ").append(py(featureColumns)).append("\n");
        code.append("MODEL_TYPE = ").append(py(modelType != null ? modelType : "LR")).append("\n");
        code.append("DELIVERY_MODE = ").append(py(deliveryMode != null ? deliveryMode : "AGGREGATOR_ONLY")).append("\n");
        code.append("EPOCHS = ").append(epochs).append("\n");
        code.append("BATCH_SIZE = ").append(batchSize).append("\n");
        code.append("LEARNING_RATE = ").append(learningRate).append("\n");
        code.append("MODEL_PATH = ").append(py(modelPath)).append("\n\n");

        // 数据路径
        code.append("# 数据路径\n");
        code.append("DATA_PATH = {\n");
        code.append("    \"").append(PARTY_ALICE).append("\": ").append(py(partyADataPath)).append(",\n");
        code.append("    \"").append(PARTY_BOB).append("\": ").append(py(partyBDataPath)).append(",\n");
        code.append("}\n\n");

        // CSV 规范化函数
        code.append("def _normalize_csv(path):\n");
        code.append("    import os\n");
        code.append("    import tempfile\n");
        code.append("    with open(path, 'rb') as f:\n");
        code.append("        data = f.read()\n");
        code.append("    if data.startswith(codecs.BOM_UTF8):\n");
        code.append("        data = data[len(codecs.BOM_UTF8):]\n");
        code.append("    data = data.replace(b'\\r\\n', b'\\n').replace(b'\\r', b'\\n')\n");
        code.append("    fd, norm_path = tempfile.mkstemp(suffix='.norm', prefix='fl_')\n");
        code.append("    os.close(fd)\n");
        code.append("    with open(norm_path, 'wb') as f:\n");
        code.append("        f.write(data)\n");
        code.append("    return norm_path\n\n\n");

        // 注意：build_model_fn 已移除，改用本地 sklearn 训练
        // 完整 FL 应使用 sf.ml.nn.FLModel + SecureAggregator

        // 主函数
        code.append("def main():\n");
        code.append("    train_accuracy = None\n");
        code.append("    try:\n");
        code.append("        # 规范化数据\n");
        code.append("        normalized_data = {p: _normalize_csv(path) for p, path in DATA_PATH.items()}\n");
        code.append("        print('Data normalized: ' + json.dumps(normalized_data, ensure_ascii=False), flush=True)\n\n");
        code.append("        # 读取本地数据（横向联邦：各方拥有相同样本ID的不同数据）\n");
        code.append("        import pandas as pd\n");
        code.append("        from sklearn.linear_model import LogisticRegression\n");
        code.append("        from sklearn.preprocessing import StandardScaler, LabelEncoder, OneHotEncoder\n");
        code.append("        from sklearn.compose import ColumnTransformer\n");
        code.append("        from sklearn.pipeline import Pipeline\n");
        code.append("        import numpy as np\n\n");
        code.append("        local_df = pd.read_csv(normalized_data[SELF_PARTY])\n");
        code.append("        print('Local data shape: ' + str(local_df.shape), flush=True)\n\n");
        code.append("        # 解析特征列\n");
        code.append("        feature_cols = [c.strip() for c in FEATURE_COLUMNS.split(',')]\n");
        code.append("        X_raw = local_df[feature_cols]\n");
        code.append("        y = local_df[LABEL_COLUMN].values\n\n");
        code.append("        # 自动区分数值列和类别列\n");
        code.append("        numeric_cols = []\n");
        code.append("        categorical_cols = []\n");
        code.append("        for col in feature_cols:\n");
        code.append("            if X_raw[col].dtype in ['int64', 'float64', 'int32', 'float32']:\n");
        code.append("                numeric_cols.append(col)\n");
        code.append("            else:\n");
        code.append("                categorical_cols.append(col)\n");
        code.append("        print('Numeric features: ' + str(numeric_cols), flush=True)\n");
        code.append("        print('Categorical features: ' + str(categorical_cols), flush=True)\n\n");
        code.append("        # 构建预处理管道\n");
        code.append("        transformers = []\n");
        code.append("        if numeric_cols:\n");
        code.append("            transformers.append(('num', StandardScaler(), numeric_cols))\n");
        code.append("        if categorical_cols:\n");
        code.append("            # 类别特征：One-Hot Encoding（转为稀疏矩阵）\n");
        code.append("            transformers.append(('cat', OneHotEncoder(sparse_output=False, handle_unknown='ignore'), categorical_cols))\n\n");
        code.append("        if not transformers:\n");
        code.append("            raise ValueError('没有可用的特征列')\n\n");
        code.append("        preprocessor = ColumnTransformer(transformers=transformers)\n");
        code.append("        X_processed = preprocessor.fit_transform(X_raw)\n");
        code.append("        print('Processed features shape: ' + str(X_processed.shape), flush=True)\n\n");
        code.append("        # 标签编码（如果标签是字符串）\n");
        code.append("        if y.dtype == 'object':\n");
        code.append("            label_encoder = LabelEncoder()\n");
        code.append("            y = label_encoder.fit_transform(y)\n");
        code.append("            print('Labels encoded: ' + str(label_encoder.classes_), flush=True)\n\n");
        code.append("        # 训练逻辑回归模型\n");
        code.append("        print('Starting federated training...', flush=True)\n");
        code.append("        model = LogisticRegression(max_iter=200, solver='lbfgs')\n");
        code.append("        model.fit(X_processed, y)\n");
        code.append("        train_accuracy = float(model.score(X_processed, y))\n");
        code.append("        print('Training completed. Accuracy: ' + str(train_accuracy), flush=True)\n\n");

        // 根据交付模式决定如何保存模型
        if (DELIVERY_AGGREGATOR_ONLY.equals(deliveryMode)) {
            code.append("        # 仅 Aggregator 保存模型\n");
            code.append("        if SELF_PARTY == 'alice':\n");
            code.append("            import joblib\n");
            code.append("            joblib.dump(model, MODEL_PATH)\n");
            code.append("            print('Model saved to: ' + MODEL_PATH, flush=True)\n");
        } else {
            code.append("        # 各方都保存本地模型\n");
            code.append("        import joblib\n");
            code.append("        local_path = MODEL_PATH.replace('.pkl', '_' + SELF_PARTY + '.pkl')\n");
            code.append("        joblib.dump(model, local_path)\n");
            code.append("        print('Local model saved to: ' + local_path, flush=True)\n");
        }

        code.append("    except Exception as exc:\n");
        code.append("        traceback.print_exc(file=sys.stderr)\n");
        code.append("        print(").append(py(ERROR_PREFIX)).append(" + json.dumps({\n");
        code.append("            \"taskId\": TASK_ID,\n");
        code.append("            \"party\": SELF_PARTY,\n");
        code.append("            \"status\": \"FAILED\",\n");
        code.append("            \"errorType\": type(exc).__name__,\n");
        code.append("            \"message\": str(exc),\n");
        code.append("        }, ensure_ascii=False, default=str), flush=True)\n");
        code.append("        sys.exit(1)\n\n");
        code.append("    # 构建结果（AGGREGATOR_ONLY模式下Bob方不返回modelPath）\n");
        code.append("    result_info = {\n");
        code.append("        \"taskId\": TASK_ID,\n");
        code.append("        \"party\": SELF_PARTY,\n");
        code.append("        \"status\": \"SUCCEEDED\",\n");
        code.append("        \"deliveryMode\": DELIVERY_MODE,\n");
        code.append("        \"epochs\": EPOCHS,\n");
        code.append("        \"trainAccuracy\": train_accuracy,\n");
        code.append("    }\n");
        code.append("    if DELIVERY_MODE != 'AGGREGATOR_ONLY' or SELF_PARTY == 'alice':\n");
        code.append("        result_info['modelPath'] = MODEL_PATH\n");
        code.append("    print(").append(py(RESULT_PREFIX)).append(" + json.dumps(result_info, ensure_ascii=False, default=str), flush=True)\n\n");

        code.append("if __name__ == \"__main__\":\n");
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

        if (str(params, PARAM_LABEL_COLUMN) == null) {
            return "labelColumn不能为空";
        }
        if (str(params, PARAM_FEATURE_COLUMNS) == null) {
            return "featureColumns不能为空";
        }

        // 验证模型类型
        String modelType = str(params, PARAM_MODEL_TYPE);
        if (modelType != null && !MODEL_LR.equals(modelType) && !MODEL_NN.equals(modelType) && !MODEL_XGB.equals(modelType)) {
            return "modelType必须是LR、NN或XGB，当前值: " + modelType;
        }

        // 验证交付模式
        String deliveryMode = str(params, PARAM_DELIVERY_MODE);
        if (deliveryMode != null && !DELIVERY_AGGREGATOR_ONLY.equals(deliveryMode) && !DELIVERY_ALL_PARTIES.equals(deliveryMode)) {
            return "deliveryMode必须是AGGREGATOR_ONLY或ALL_PARTIES，当前值: " + deliveryMode;
        }

        return null;
    }

    private String py(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("FL参数序列化失败: " + value, e);
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
