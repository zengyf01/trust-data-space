package com.tds.dos.service.msp.auth;

/**
 * Login request DTO
 */
public class AuthDTO {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
