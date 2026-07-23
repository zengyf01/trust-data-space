package com.tds.service.dataspace;

/**
 * 数据空间DTO
 */
public class DataSpaceDTO {

    private String id;
    private String spaceCode;
    private String spaceName;
    private String spaceDesc;
    private String ownerId;
    private String ownerName;
    private String organizationId;
    private String organizationName;
    private Integer status;
    private String spaceType;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSpaceCode() { return spaceCode; }
    public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
    public String getSpaceName() { return spaceName; }
    public void setSpaceName(String spaceName) { this.spaceName = spaceName; }
    public String getSpaceDesc() { return spaceDesc; }
    public void setSpaceDesc(String spaceDesc) { this.spaceDesc = spaceDesc; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getSpaceType() { return spaceType; }
    public void setSpaceType(String spaceType) { this.spaceType = spaceType; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}