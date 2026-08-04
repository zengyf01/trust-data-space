package com.tds.dos.service.privacycompute.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * PIR (Private Information Retrieval) 配置生成器
 * <p>
 * 生成 secretflow/psi-anolis8 镜像的 C++ 二进制配置文件（JSON）。
 * 支持两种协议：
 * <ul>
 *   <li>SealPIR：Label PIR，直接取多列值（适合 id→name,email 场景）</li>
 *   <li>APSI：Keyword PIR，只判断 key 是否存在（需扩展才能做 label PIR）</li>
 * </ul>
 * <p>
 * 生成器是纯函数：不查库、不调用 Agent，只做 params -> JSON 字符串。
 */
@Slf4j
@Component
public class PirCodeGenerator implements ICodeGenerator {

    /** 结果摘要前缀 */
    public static final String RESULT_PREFIX = "TDS_PIR_RESULT=";
    /** 错误摘要前缀 */
    public static final String ERROR_PREFIX = "TDS_PIR_ERROR=";

    // 参数名常量
    public static final String PARAM_PIR_TYPE = "pirType";
    public static final String PARAM_SELF_PARTY = "selfParty";
    public static final String PARAM_SERVER_DATA_PATH = "serverDataPath";
    public static final String PARAM_KEY_COLUMN = "keyColumn";
    public static final String PARAM_LABEL_COLUMNS = "labelColumns";
    public static final String PARAM_QUERY_COLUMN = "queryColumn";
    public static final String PARAM_QUERY_VALUE = "queryValue";
    public static final String PARAM_OPRF_KEY_PATH = "oprfKeyPath";
    public static final String PARAM_OPRF_KEY_SAVE_PATH = "oprfKeySavePath";
    public static final String PARAM_NUM_PER_QUERY = "numPerQuery";
    public static final String PARAM_LABEL_MAX_LEN = "labelMaxLen";
    public static final String PARAM_PHASE = "phase";  // "setup" or "query"

    // 角色常量
    public static final String PARTY_ALICE = "alice";  // 客户端（发起查询）
    public static final String PARTY_BOB = "bob";       // 服务端（持有数据）

    // 协议常量
    public static final String PROTOCOL_SEALPIR = "SealPIR";
    public static final String PROTOCOL_APSI = "APSI";

    // 默认值
    private static final int DEFAULT_NUM_PER_QUERY = 1;
    private static final int DEFAULT_LABEL_MAX_LEN = 256;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Pattern HOST_PORT = Pattern.compile("^[^\\s:/]+:\\d{1,5}$");

    @Override
    public String getTaskType() {
        return "PIR";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        String error = validateParams(params);
        if (error != null) {
            throw new IllegalArgumentException("PIR参数校验失败: " + error);
        }

        String pirType = str(params, PARAM_PIR_TYPE);
        String selfParty = str(params, PARAM_SELF_PARTY).toLowerCase(Locale.ROOT);
        String phase = str(params, PARAM_PHASE);
        String serverDataPath = str(params, PARAM_SERVER_DATA_PATH);
        String keyColumn = str(params, PARAM_KEY_COLUMN);
        String labelColumnsStr = str(params, PARAM_LABEL_COLUMNS);
        String oprfKeyPath = str(params, PARAM_OPRF_KEY_PATH);
        String oprfKeySavePath = str(params, PARAM_OPRF_KEY_SAVE_PATH);
        String queryColumn = str(params, PARAM_QUERY_COLUMN);
        String queryValue = str(params, PARAM_QUERY_VALUE);

        int numPerQuery = DEFAULT_NUM_PER_QUERY;
        if (params.get(PARAM_NUM_PER_QUERY) != null) {
            numPerQuery = Integer.parseInt(String.valueOf(params.get(PARAM_NUM_PER_QUERY)));
        }
        int labelMaxLen = DEFAULT_LABEL_MAX_LEN;
        if (params.get(PARAM_LABEL_MAX_LEN) != null) {
            labelMaxLen = Integer.parseInt(String.valueOf(params.get(PARAM_LABEL_MAX_LEN)));
        }

        // 解析 label columns
        String[] labelColumns = labelColumnsStr != null ? labelColumnsStr.split(",") : new String[0];

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("task_id", taskId);
        config.put("protocol", pirType);
        config.put("self_party", selfParty);
        config.put("generate_time", LocalDateTime.now().toString());

        if (PROTOCOL_SEALPIR.equalsIgnoreCase(pirType)) {
            if ("setup".equals(phase)) {
                config.put("run_mode", "setup");
                buildSealPirSetup(config, taskId, serverDataPath, keyColumn, labelColumns,
                    oprfKeyPath, oprfKeySavePath, numPerQuery, labelMaxLen);
            } else {
                config.put("run_mode", "query");
                buildSealPirQuery(config, taskId, keyColumn, queryColumn, queryValue, oprfKeyPath);
            }
        } else if (PROTOCOL_APSI.equalsIgnoreCase(pirType)) {
            if ("setup".equals(phase)) {
                config.put("run_mode", "setup");
                buildApsiSetup(config, taskId, serverDataPath, keyColumn, labelColumns, oprfKeySavePath);
            } else {
                config.put("run_mode", "query");
                buildApsiQuery(config, taskId, keyColumn, queryColumn, queryValue, oprfKeyPath);
            }
        } else {
            throw new IllegalArgumentException("不支持的 PIR 协议: " + pirType + "，仅支持 SealPIR/APSI");
        }

        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("生成PIR JSON配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 SealPIR Setup 配置
     */
    private void buildSealPirSetup(Map<String, Object> config, String taskId,
                                   String serverDataPath, String keyColumn, String[] labelColumns,
                                   String oprfKeyPath, String oprfKeySavePath,
                                   int numPerQuery, int labelMaxLen) {
        Map<String, Object> setup = new LinkedHashMap<>();
        setup.put("data_path", serverDataPath);
        setup.put("key_column", keyColumn);
        setup.put("label_columns", labelColumns);
        setup.put("oprf_key_path", oprfKeyPath);
        setup.put("oprf_key_save_path", oprfKeySavePath != null ? oprfKeySavePath : oprfKeyPath);
        setup.put("num_per_query", numPerQuery);
        setup.put("label_max_len", labelMaxLen);
        config.put("setup", setup);
    }

    /**
     * 构建 SealPIR Query 配置
     */
    private void buildSealPirQuery(Map<String, Object> config, String taskId,
                                   String keyColumn, String queryColumn,
                                   String queryValue, String oprfKeyPath) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("query_column", queryColumn);
        query.put("query_values", new String[]{queryValue});
        query.put("oprf_key_path", oprfKeyPath);
        config.put("query", query);
    }

    /**
     * 构建 APSI Setup 配置
     */
    private void buildApsiSetup(Map<String, Object> config, String taskId,
                                String serverDataPath, String keyColumn,
                                String[] labelColumns, String oprfKeySavePath) {
        Map<String, Object> setup = new LinkedHashMap<>();
        setup.put("data_path", serverDataPath);
        setup.put("key_column", keyColumn);
        setup.put("label_columns", labelColumns);
        if (oprfKeySavePath != null) {
            setup.put("oprf_key_save_path", oprfKeySavePath);
        }
        config.put("setup", setup);
    }

    /**
     * 构建 APSI Query 配置
     */
    private void buildApsiQuery(Map<String, Object> config, String taskId,
                                String keyColumn, String queryColumn,
                                String queryValue, String oprfKeyPath) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("query_column", queryColumn);
        query.put("query_values", new String[]{queryValue});
        if (oprfKeyPath != null) {
            query.put("oprf_key_path", oprfKeyPath);
        }
        config.put("query", query);
    }

    @Override
    public String validateParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "参数不能为空";
        }

        String pirType = str(params, PARAM_PIR_TYPE);
        if (pirType == null) {
            return "pirType不能为空（SealPIR 或 APSI）";
        }
        String normalizedPirType = pirType.toLowerCase(Locale.ROOT);
        if (!PROTOCOL_SEALPIR.toLowerCase(Locale.ROOT).equals(normalizedPirType)
            && !PROTOCOL_APSI.toLowerCase(Locale.ROOT).equals(normalizedPirType)) {
            return "pirType只能是SealPIR或APSI，当前值: " + pirType;
        }

        String selfParty = str(params, PARAM_SELF_PARTY);
        if (selfParty == null) {
            return "selfParty不能为空";
        }
        String normalizedSelfParty = selfParty.toLowerCase(Locale.ROOT);
        if (!PARTY_ALICE.equals(normalizedSelfParty) && !PARTY_BOB.equals(normalizedSelfParty)) {
            return "selfParty只能是alice或bob，当前值: " + selfParty;
        }

        String phase = str(params, PARAM_PHASE);
        if (phase == null) {
            return "phase不能为空";
        }

        if ("setup".equals(phase)) {
            if (str(params, PARAM_SERVER_DATA_PATH) == null) {
                return "serverDataPath不能为空";
            }
            if (str(params, PARAM_KEY_COLUMN) == null) {
                return "keyColumn不能为空";
            }
            // labelColumns 可选（APSI 可能不需要）
        } else if ("query".equals(phase)) {
            if (str(params, PARAM_QUERY_VALUE) == null) {
                return "queryValue不能为空";
            }
        } else {
            return "phase必须是setup或query，当前值: " + phase;
        }

        return null;
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
