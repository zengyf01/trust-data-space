package com.tds.service.dataspace;

/**
 * 数据空间资源DTO
 */
public class DataSpaceResourceDTO {

    private String id;
    private String spaceId;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String resourceDesc;
    private Integer accessLevel;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getResourceDesc() { return resourceDesc; }
    public void setResourceDesc(String resourceDesc) { this.resourceDesc = resourceDesc; }
    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}