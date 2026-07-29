package com.tds.dos.service.psi;

import java.util.Map;

/**
 * PSI代码生成器接口
 * 根据任务参数生成SecretFlow Python代码
 */
public interface IPsiCodeGenerator {

    /**
     * 生成SecretFlow PSI Python代码（使用配置文件中的默认地址）
     * @param taskId 任务ID
     * @param partyADataPath 参与方A数据路径
     * @param partyBDataPath 参与方B数据路径
     * @param keyColumn 关联键列名
     * @param protocol PSI协议 (ECPSI, KKRTPSI, RR22PSI)
     * @param resultType 结果类型 (INTERSECTION, UNION, ...)
     * @param role 角色 (A 或 B)
     * @return 生成的Python代码
     */
    String generatePsiCode(String taskId, String partyADataPath, String partyBDataPath,
                          String keyColumn, String protocol, String resultType, String role);

    /**
     * 生成SecretFlow PSI Python代码（使用指定的节点地址）
     * @param taskId 任务ID
     * @param partyADataPath 参与方A数据路径
     * @param partyBDataPath 参与方B数据路径
     * @param keyColumn 关联键列名
     * @param protocol PSI协议 (ECPSI, KKRTPSI, RR22PSI)
     * @param resultType 结果类型 (INTERSECTION, UNION, ...)
     * @param role 角色 (A 或 B)
     * @param rayAddress Ray集群地址
     * @param partyAAddress 参与方A的Ray节点地址
     * @param partyBAddress 参与方B的Ray节点地址
     * @return 生成的Python代码
     */
    String generatePsiCode(String taskId, String partyADataPath, String partyBDataPath,
                          String keyColumn, String protocol, String resultType, String role,
                          String rayAddress, String partyAAddress, String partyBAddress);

    /**
     * 生成完整的多方PSI执行脚本
     * @param taskId 任务ID
     * @param params 所有参数
     * @return 生成的Python代码
     */
    String generateMultiPartyPsiCode(String taskId, Map<String, String> params);
}
