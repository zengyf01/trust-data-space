package com.tds.service.organization;

/**
 * 角色DTO
 */
public class RoleDTO {

    private String id;
    private String roleCode;
    private String roleName;
    private String roleType;
    private String orgId;
    private String roleDesc;
    private Integer isSystem;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getRoleDesc() { return roleDesc; }
    public void setRoleDesc(String roleDesc) { this.roleDesc = roleDesc; }
    public Integer getIsSystem() { return isSystem; }
    public void setIsSystem(Integer isSystem) { this.isSystem = isSystem; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}