package com.tds.dos.msp.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.dal.entity.TbMspUser;

import java.util.List;

/**
 * User Service interface
 */
public interface IUserService {

    /**
     * Create a new user
     */
    TbMspUser createUser(CreateUserRequest request);

    /**
     * Update an existing user
     */
    TbMspUser updateUser(String userId, UpdateUserRequest request);

    /**
     * Delete a user (soft delete)
     */
    void deleteUser(String userId);

    /**
     * Get user by ID
     */
    TbMspUser getUserById(String userId);

    /**
     * Get user by username
     */
    TbMspUser getUserByUsername(String username);

    /**
     * Get all users with pagination
     */
    IPage<TbMspUser> getUserPage(int currentPage, int pageSize, String keyword);

    /**
     * Get all users
     */
    List<TbMspUser> getAllUsers();

    /**
     * Enable or disable user
     */
    TbMspUser setUserEnabled(String userId, boolean enabled);

    /**
     * Reset user password
     */
    void resetPassword(String userId, String newPassword);

    /**
     * Check if username exists
     */
    boolean usernameExists(String username);
}