package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 数字合约实体
 */
@TableName("tb_digital_contract")
public class TbDigitalContract {

    @TableId
    private String id;

    /** 订单编号 */
    private String orderCode;

    /** 合约编号 */
    private String contractCode;

    /** 合约类型 */
    private Integer contractType;

    /** 合约状态(1待签,2签署中,3执行,4拒绝,5终止) */
    private Integer contractStatus;

    /** 合约摘要(SHA256) */
    private String contractAbstract;

    /** 合约开始时间 */
    private LocalDateTime contractStartTime;

    /** 合约结束时间 */
    private LocalDateTime contractEndTime;

    /** 合约JSON */
    private String contractJson;

    /** 资源快照 */
    private String resourceSnapshot;

    /** 应用ID */
    private String appid;

    /** 应用密钥 */
    private String appkey;

    /** ====== 供应方信息 ====== */
    private String providerInstitutionId;
    private String providerInstitutionName;
    private String providerContactName;
    private String providerPhone;
    private String providerEmail;
    private String providerConnectorAddress;
    private String providerInstitutionAddress;
    private String providerPublicKey;
    private String providerSignature;
    private LocalDateTime providerSignTime;

    /** ====== 使用方信息 ====== */
    private String useInstitutionId;
    private String useInstitutionName;
    private String useContactName;
    private String usePhone;
    private String useEmail;
    private String useConnectorAddress;
    private String useInstitutionAddress;
    private String usePublicKey;
    private String useSignature;
    private LocalDateTime useSignTime;

    /** 租户ID */
    private String fTenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    /** 删除标志(0未删,1已删) */
    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }
    public Integer getContractType() { return contractType; }
    public void setContractType(Integer contractType) { this.contractType = contractType; }
    public Integer getContractStatus() { return contractStatus; }
    public void setContractStatus(Integer contractStatus) { this.contractStatus = contractStatus; }
    public String getContractAbstract() { return contractAbstract; }
    public void setContractAbstract(String contractAbstract) { this.contractAbstract = contractAbstract; }
    public LocalDateTime getContractStartTime() { return contractStartTime; }
    public void setContractStartTime(LocalDateTime contractStartTime) { this.contractStartTime = contractStartTime; }
    public LocalDateTime getContractEndTime() { return contractEndTime; }
    public void setContractEndTime(LocalDateTime contractEndTime) { this.contractEndTime = contractEndTime; }
    public String getContractJson() { return contractJson; }
    public void setContractJson(String contractJson) { this.contractJson = contractJson; }
    public String getResourceSnapshot() { return resourceSnapshot; }
    public void setResourceSnapshot(String resourceSnapshot) { this.resourceSnapshot = resourceSnapshot; }
    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }
    public String getAppkey() { return appkey; }
    public void setAppkey(String appkey) { this.appkey = appkey; }
    public String getProviderInstitutionId() { return providerInstitutionId; }
    public void setProviderInstitutionId(String providerInstitutionId) { this.providerInstitutionId = providerInstitutionId; }
    public String getProviderInstitutionName() { return providerInstitutionName; }
    public void setProviderInstitutionName(String providerInstitutionName) { this.providerInstitutionName = providerInstitutionName; }
    public String getProviderContactName() { return providerContactName; }
    public void setProviderContactName(String providerContactName) { this.providerContactName = providerContactName; }
    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }
    public String getProviderConnectorAddress() { return providerConnectorAddress; }
    public void setProviderConnectorAddress(String providerConnectorAddress) { this.providerConnectorAddress = providerConnectorAddress; }
    public String getProviderInstitutionAddress() { return providerInstitutionAddress; }
    public void setProviderInstitutionAddress(String providerInstitutionAddress) { this.providerInstitutionAddress = providerInstitutionAddress; }
    public String getProviderPublicKey() { return providerPublicKey; }
    public void setProviderPublicKey(String providerPublicKey) { this.providerPublicKey = providerPublicKey; }
    public String getProviderSignature() { return providerSignature; }
    public void setProviderSignature(String providerSignature) { this.providerSignature = providerSignature; }
    public LocalDateTime getProviderSignTime() { return providerSignTime; }
    public void setProviderSignTime(LocalDateTime providerSignTime) { this.providerSignTime = providerSignTime; }
    public String getUseInstitutionId() { return useInstitutionId; }
    public void setUseInstitutionId(String useInstitutionId) { this.useInstitutionId = useInstitutionId; }
    public String getUseInstitutionName() { return useInstitutionName; }
    public void setUseInstitutionName(String useInstitutionName) { this.useInstitutionName = useInstitutionName; }
    public String getUseContactName() { return useContactName; }
    public void setUseContactName(String useContactName) { this.useContactName = useContactName; }
    public String getUsePhone() { return usePhone; }
    public void setUsePhone(String usePhone) { this.usePhone = usePhone; }
    public String getUseEmail() { return useEmail; }
    public void setUseEmail(String useEmail) { this.useEmail = useEmail; }
    public String getUseConnectorAddress() { return useConnectorAddress; }
    public void setUseConnectorAddress(String useConnectorAddress) { this.useConnectorAddress = useConnectorAddress; }
    public String getUseInstitutionAddress() { return useInstitutionAddress; }
    public void setUseInstitutionAddress(String useInstitutionAddress) { this.useInstitutionAddress = useInstitutionAddress; }
    public String getUsePublicKey() { return usePublicKey; }
    public void setUsePublicKey(String usePublicKey) { this.usePublicKey = usePublicKey; }
    public String getUseSignature() { return useSignature; }
    public void setUseSignature(String useSignature) { this.useSignature = useSignature; }
    public LocalDateTime getUseSignTime() { return useSignTime; }
    public void setUseSignTime(LocalDateTime useSignTime) { this.useSignTime = useSignTime; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}