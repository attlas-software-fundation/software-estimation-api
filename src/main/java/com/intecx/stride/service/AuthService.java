package com.intecx.stride.service;

import com.intecx.stride.dto.request.auth.ChangePasswordRequest;
import com.intecx.stride.dto.request.auth.LoginRequest;
import com.intecx.stride.dto.request.auth.LogoutRequest;
import com.intecx.stride.dto.request.auth.RefreshTokenRequest;
import com.intecx.stride.dto.request.auth.RequestPasswordResetRequest;
import com.intecx.stride.dto.request.auth.ResetPasswordRequest;
import com.intecx.stride.dto.request.auth.UpdateProfileRequest;
import com.intecx.stride.dto.request.auth.VerifyResetCodeRequest;
import com.intecx.stride.dto.response.AuthResponse;
import com.intecx.stride.dto.response.MessageResponse;
import com.intecx.stride.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    MessageResponse logout(String bearerToken, LogoutRequest request);
    UserResponse getCurrentProfile(String email);
    UserResponse updateCurrentProfile(String currentEmail, UpdateProfileRequest request);
    MessageResponse changePassword(String currentEmail, ChangePasswordRequest request);
    MessageResponse requestPasswordReset(RequestPasswordResetRequest request);
    MessageResponse verifyResetCode(VerifyResetCodeRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}
