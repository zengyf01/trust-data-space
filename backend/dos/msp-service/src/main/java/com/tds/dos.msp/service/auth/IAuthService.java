package com.tds.dos.msp.service.auth;

import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.dal.entity.TbMspUser;

/**
 * Auth Service interface
 */
public interface IAuthService {
    ApiResponse<LoginResponse> login(AuthDTO request);
    ApiResponse<Void> logout(String token);
    TbMspUser getCurrentUser(String userId);
}