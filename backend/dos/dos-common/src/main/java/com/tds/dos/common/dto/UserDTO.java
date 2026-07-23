package com.tds.dos.common.dto;

import java.time.LocalDateTime;

/**
 * User DTO for API
 */
public class UserDTO {

    private String userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private Integer roleCode;
    private String roleName;
    private String status;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UserDTO() {}

    public UserDTO(String userId, String username, String email, String phone,
                   String role, Integer enabled) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.enabled = enabled;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getRoleCode() { return roleCode; }
    public void setRoleCode(Integer roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}