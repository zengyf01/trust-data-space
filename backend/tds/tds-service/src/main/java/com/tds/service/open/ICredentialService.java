package com.tds.service.open;

import com.tds.dal.entity.TbMemberCredential;
import java.util.Map;

/**
 * 机构凭证服务接口
 */
public interface ICredentialService {

    /**
     * 颁发凭证
     * @param memberId 机构ID
     * @param connectorNumber 连接器编号
     * @param credentialType 凭证类型 API_KEY / SM2_CERT
     * @param expireDays 有效期天数
     * @return 凭证信息
     */
    Map<String, String> issueCredential(String memberId, String connectorNumber,
                                         String credentialType, int expireDays);

    /**
     * 验证API_KEY凭证
     * @param appId 应用ID
     * @param appKey 应用密钥
     * @return 验证结果
     */
    boolean verifyApiKey(String appId, String appKey);

    /**
     * 验证SM2签名
     * @param appId 应用ID
     * @param timestamp 时间戳
     * @param signData 签名原文
     * @param signature 签名值
     * @return 验证结果
     */
    boolean verifySm2Signature(String appId, String timestamp, String signData, String signature);

    /**
     * 根据AppId获取凭证
     * @param appId 应用ID
     * @return 凭证实体
     */
    TbMemberCredential getByAppId(String appId);

    /**
     * 作废凭证
     * @param appId 应用ID
     */
    void revokeCredential(String appId);

    /**
     * 刷新凭证
     * @param appId 应用ID
     * @param expireDays 新有效期天数
     * @return 新凭证信息
     */
    Map<String, String> refreshCredential(String appId, int expireDays);

    /**
     * 获取机构的有效凭证列表
     * @param memberId 机构ID
     * @return 凭证列表
     */
    java.util.List<TbMemberCredential> getValidCredentials(String memberId);
}