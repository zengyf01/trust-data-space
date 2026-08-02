package com.tds.dos.service.privacycompute.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * PSI (Private Set Intersection) 代码生成器
 * <p>
 * 生成 SecretFlow PRODUCTION 模式（ray_mode=False）的两方 PSI 脚本。
 * 节点上安装的是 secretflow-lite，只注册了 PRODUCTION / DEBUG 两种分布式模式，
 * SIMULATION 与 RAY_PRODUCTION 需要 secretflow_fl 包（节点上为空目录），因此
 * 必须显式传 ray_mode=False，否则会报 DISTRIBUTION_MODE.RAY_PRODUCTION not registered。
 * <p>
 * 生成器是纯函数：不查库、不调用 Agent，只做 params -> Python 字符串。
 * alice 与 bob 两份脚本只有 SELF_PARTY 不同，拓扑配置必须完全一致。
 */
@Slf4j
@Component
public class PsiCodeGenerator implements ICodeGenerator {

    /** 结果摘要前缀，脚本把它作为最后一条 stdout 输出，DOS 侧据此解析 */
    public static final String RESULT_PREFIX = "TDS_PSI_RESULT=";
    /** 错误摘要前缀 */
    public static final String ERROR_PREFIX = "TDS_PSI_ERROR=";

    public static final String PARAM_SELF_PARTY = "selfParty";
    public static final String PARAM_PARTY_A_DATA_PATH = "partyADataPath";
    public static final String PARAM_PARTY_B_DATA_PATH = "partyBDataPath";
    public static final String PARAM_KEY_COLUMN = "keyColumn";
    public static final String PARAM_PROTOCOL = "protocol";
    public static final String PARAM_RESULT_TYPE = "resultType";
    public static final String PARAM_RECEIVER = "receiver";
    public static final String PARAM_PARTY_A_CROSS_SILO_ADDRESS = "partyACrossSiloAddress";
    public static final String PARAM_PARTY_B_CROSS_SILO_ADDRESS = "partyBCrossSiloAddress";
    public static final String PARAM_PARTY_A_SPU_ADDRESS = "partyASpuAddress";
    public static final String PARAM_PARTY_B_SPU_ADDRESS = "partyBSpuAddress";

    public static final String PARTY_ALICE = "alice";
    public static final String PARTY_BOB = "bob";

    /** SPU 的 PSI 是 inner join，只支持求交 */
    private static final String RESULT_TYPE_INTERSECTION = "INTERSECTION";
    private static final String SPU_PROTOCOL_ECDH = "PROTOCOL_ECDH";
    private static final Pattern HOST_PORT = Pattern.compile("^[^\\s:/]+:\\d{1,5}$");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getTaskType() {
        return "PSI";
    }

    @Override
    public String generateCode(String taskId, Map<String, Object> params) {
        String error = validateParams(params);
        if (error != null) {
            throw new IllegalArgumentException("PSI参数校验失败: " + error);
        }

        String selfParty = str(params, PARAM_SELF_PARTY).toLowerCase(Locale.ROOT);
        String keyColumn = str(params, PARAM_KEY_COLUMN);
        String protocol = str(params, PARAM_PROTOCOL);
        String spuProtocol = mapProtocol(protocol);
        String receiver = params.get(PARAM_RECEIVER) != null
            ? str(params, PARAM_RECEIVER).toLowerCase(Locale.ROOT) : PARTY_ALICE;

        String aliceCrossSilo = str(params, PARAM_PARTY_A_CROSS_SILO_ADDRESS);
        String bobCrossSilo = str(params, PARAM_PARTY_B_CROSS_SILO_ADDRESS);
        String aliceSpu = str(params, PARAM_PARTY_A_SPU_ADDRESS);
        String bobSpu = str(params, PARAM_PARTY_B_SPU_ADDRESS);

        String aliceInput = str(params, PARAM_PARTY_A_DATA_PATH);
        String bobInput = str(params, PARAM_PARTY_B_DATA_PATH);
        String aliceOutput = outputPath(taskId, PARTY_ALICE);
        String bobOutput = outputPath(taskId, PARTY_BOB);

        StringBuilder code = new StringBuilder();
        code.append("#!/usr/bin/env python3\n");
        code.append("# -*- coding: utf-8 -*-\n");
        code.append("#\n");
        code.append("# PSI任务执行脚本 - TaskID: ").append(taskId).append("\n");
        code.append("# 生成时间: ").append(LocalDateTime.now()).append("\n");
        code.append("# 协议: ").append(protocol).append(" -> ").append(spuProtocol).append("\n");
        code.append("# 本方角色: ").append(selfParty).append("\n");
        code.append("#\n");
        code.append("# SecretFlow PRODUCTION 模式(ray_mode=False)：两方各自在本节点独立运行本脚本，\n");
        code.append("# 双方脚本只有 SELF_PARTY 不同，通过 cluster_config 中的地址互连，不依赖 Ray。\n");
        code.append("#\n\n");

        code.append("import codecs\n");
        code.append("import json\n");
        code.append("import sys\n");
        code.append("import traceback\n\n");
        code.append("import secretflow as sf\n\n");

        code.append("TASK_ID = ").append(py(taskId)).append("\n");
        code.append("SELF_PARTY = ").append(py(selfParty)).append("\n");
        code.append("PROTOCOL = ").append(py(spuProtocol)).append("\n");
        code.append("RECEIVER = ").append(py(receiver)).append("\n\n");

        code.append("# 跨域(cross-silo)通信配置：address 为对端可达地址，listen_addr 绑定本机全部网卡\n");
        code.append("CLUSTER_CONFIG = {\n");
        code.append("    \"parties\": {\n");
        appendParty(code, PARTY_ALICE, aliceCrossSilo);
        appendParty(code, PARTY_BOB, bobCrossSilo);
        code.append("    },\n");
        code.append("    \"self_party\": SELF_PARTY,\n");
        code.append("}\n\n");

        code.append("# SPU 设备配置，与 cross-silo 必须使用不同端口\n");
        code.append("SPU_CLUSTER_DEF = {\n");
        code.append("    \"nodes\": [\n");
        appendSpuNode(code, PARTY_ALICE, aliceSpu);
        appendSpuNode(code, PARTY_BOB, bobSpu);
        code.append("    ],\n");
        code.append("    \"runtime_config\": {\"protocol\": \"SEMI2K\", \"field\": \"FM128\"},\n");
        code.append("}\n\n");

        code.append("KEYS = {\"alice\": [").append(py(keyColumn)).append("], ");
        code.append("\"bob\": [").append(py(keyColumn)).append("]}\n");
        code.append("INPUT_PATH = {\"alice\": ").append(py(aliceInput));
        code.append(", \"bob\": ").append(py(bobInput)).append("}\n");
        code.append("OUTPUT_PATH = {\"alice\": ").append(py(aliceOutput));
        code.append(", \"bob\": ").append(py(bobOutput)).append("}\n\n");

        // 剥 UTF-8 BOM + CRLF 归一为 LF，避免 spu::GetCsvColumnsNames 读不到 key column。
        // 使用 tempfile 避免 /data 目录不存在或无写权限的问题
        code.append("def _normalize_csv(path):\n");
        code.append("    import os\n");
        code.append("    import tempfile\n");
        code.append("    with open(path, 'rb') as f:\n");
        code.append("        data = f.read()\n");
        code.append("    if data.startswith(codecs.BOM_UTF8):\n");
        code.append("        data = data[len(codecs.BOM_UTF8):]\n");
        code.append("    data = data.replace(b'\\r\\n', b'\\n').replace(b'\\r', b'\\n')\n");
        code.append("    fd, norm_path = tempfile.mkstemp(suffix='.norm', prefix='psi_')\n");
        code.append("    os.close(fd)\n");
        code.append("    with open(norm_path, 'wb') as f:\n");
        code.append("        f.write(data)\n");
        code.append("    return norm_path\n\n\n");

        code.append("def _shutdown():\n");
        code.append("    try:\n");
        code.append("        sf.shutdown()\n");
        code.append("    except Exception:\n");
        code.append("        pass\n\n\n");

        code.append("def main():\n");
        code.append("    try:\n");
        code.append("        # 先规范化输入 CSV，避免 BOM / CRLF 让 SPU 后处理读表头失败\n");
        code.append("        _normalized = {p: _normalize_csv(q) for p, q in INPUT_PATH.items()}\n");
        code.append("        INPUT_PATH.clear()\n");
        code.append("        INPUT_PATH.update(_normalized)\n");
        code.append("        print('CSV normalized: ' + json.dumps(INPUT_PATH, ensure_ascii=False), flush=True)\n");
        code.append("        sf.init(cluster_config=CLUSTER_CONFIG, ray_mode=False, log_to_driver=False)\n");
        code.append("        spu_device = sf.SPU(SPU_CLUSTER_DEF)\n");
        code.append("        statistics = spu_device.psi(\n");
        code.append("            keys=KEYS,\n");
        code.append("            input_path=INPUT_PATH,\n");
        code.append("            output_path=OUTPUT_PATH,\n");
        code.append("            receiver=RECEIVER,\n");
        code.append("            protocol=PROTOCOL,\n");
        if (SPU_PROTOCOL_ECDH.equals(spuProtocol)) {
            code.append("            ecdh_curve=\"CURVE_25519\",\n");
        }
        code.append("            broadcast_result=False,\n");
        code.append("        )\n");
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
        code.append("    # 摘要必须是最后一条 stdout：Agent 只回传 stdout 末尾片段\n");
        code.append("    _shutdown()\n");
        code.append("    print(").append(py(RESULT_PREFIX)).append(" + json.dumps({\n");
        code.append("        \"taskId\": TASK_ID,\n");
        code.append("        \"party\": SELF_PARTY,\n");
        code.append("        \"status\": \"SUCCEEDED\",\n");
        code.append("        \"protocol\": PROTOCOL,\n");
        code.append("        \"outputPath\": OUTPUT_PATH[SELF_PARTY],\n");
        code.append("        \"statistics\": statistics,\n");
        code.append("    }, ensure_ascii=False, default=str), flush=True)\n\n\n");

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

        for (String key : new String[]{PARAM_PARTY_A_DATA_PATH, PARAM_PARTY_B_DATA_PATH, PARAM_KEY_COLUMN}) {
            if (str(params, key) == null) {
                return key + "不能为空";
            }
        }

        for (String key : new String[]{PARAM_PARTY_A_CROSS_SILO_ADDRESS, PARAM_PARTY_B_CROSS_SILO_ADDRESS,
            PARAM_PARTY_A_SPU_ADDRESS, PARAM_PARTY_B_SPU_ADDRESS}) {
            String address = str(params, key);
            if (address == null) {
                return key + "不能为空";
            }
            if (!HOST_PORT.matcher(address).matches()) {
                return key + "必须是host:port格式，当前值: " + address;
            }
        }

        String protocol = str(params, PARAM_PROTOCOL);
        if (protocol == null) {
            return "protocol不能为空";
        }
        try {
            mapProtocol(protocol);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        String resultType = str(params, PARAM_RESULT_TYPE);
        if (resultType != null && !RESULT_TYPE_INTERSECTION.equalsIgnoreCase(resultType)) {
            return "SPU的PSI为inner join，只支持INTERSECTION，不支持: " + resultType;
        }

        String receiver = str(params, PARAM_RECEIVER);
        if (receiver != null) {
            String normalizedReceiver = receiver.toLowerCase(Locale.ROOT);
            if (!PARTY_ALICE.equals(normalizedReceiver) && !PARTY_BOB.equals(normalizedReceiver)) {
                return "receiver只能是alice或bob，当前值: " + receiver;
            }
        }

        return null;
    }

    /**
     * 业务协议名到 SPU 协议名的映射，不支持的值直接拒绝，避免静默回退到非预期协议
     */
    private String mapProtocol(String protocol) {
        switch (protocol.toUpperCase(Locale.ROOT)) {
            case "ECPSI":
                return SPU_PROTOCOL_ECDH;
            case "KKRTPSI":
                return "PROTOCOL_KKRT";
            case "RR22PSI":
                return "PROTOCOL_RR22";
            default:
                throw new IllegalArgumentException("不支持的PSI协议: " + protocol);
        }
    }

    private String outputPath(String taskId, String party) {
        return "/tmp/psi_result_" + taskId.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + party + ".csv";
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

    /**
     * 把字符串序列化成合法的 Python 字符串字面量。
     * JSON 字符串同时也是合法的 Python 字面量，借此避免路径/列名中的引号破坏脚本。
     */
    private String py(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("PSI参数序列化失败: " + value, e);
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
