package com.tds.dos.service.msp.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.dal.msp.entity.TbUser;

import java.util.List;

/**
 * User Service interface
 */
public interface IUserService {

    /**
     * Create a new user
     */
    TbUser createUser(CreateUserRequest request);

    /**
     * Update an existing user
     */
    TbUser updateUser(String userId, UpdateUserRequest request);

    /**
     * Delete a user (soft delete)
     */
    void deleteUser(String userId);

    /**
     * Get user by ID
     */
    TbUser getUserById(String userId);

    /**
     * Get user by username
     */
    TbUser getUserByUsername(String username);

    /**
     * Get all users with pagination
     */
    IPage<TbUser> getUserPage(int currentPage, int pageSize, String keyword);

    /**
     * Get all users
     */
    List<TbUser> getAllUsers();

    /**
     * Enable or disable user
     */
    TbUser setUserEnabled(String userId, boolean enabled);

    /**
     * Reset user password
     */
    void resetPassword(String userId, String newPassword);

    /**
     * Check if username exists
     */
    boolean usernameExists(String username);
}
