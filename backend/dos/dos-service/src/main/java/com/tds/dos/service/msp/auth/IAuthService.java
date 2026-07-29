package com.tds.dos.service.msp.auth;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.dal.msp.entity.TbUser;

/**
 * Auth Service interface
 */
public interface IAuthService {
    ApiResponse<LoginResponse> login(AuthDTO request);
    ApiResponse<Void> logout(String token);
    TbUser getCurrentUser(String userId);
}
