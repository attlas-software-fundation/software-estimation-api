package com.intecx.stride.service;

import com.intecx.stride.common.PaginatedResponse;
import com.intecx.stride.common.QueryRequest;
import com.intecx.stride.dto.request.user.CreateUserRequest;
import com.intecx.stride.dto.request.user.ToggleUserStatusRequest;
import com.intecx.stride.dto.request.user.UpdateUserRequest;
import com.intecx.stride.dto.response.MessageResponse;
import com.intecx.stride.dto.response.UserResponse;
import com.intecx.stride.dto.response.UserSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    PaginatedResponse<UserResponse> search(QueryRequest query);
    List<UserSummaryResponse> listResponsibles();
    UserResponse getById(UUID id);
    UserResponse create(CreateUserRequest request, UUID actorId);
    UserResponse update(UUID id, UpdateUserRequest request, UUID actorId);
    UserResponse changeStatus(UUID id, ToggleUserStatusRequest request, UUID actorId);
    MessageResponse resendInvitation(UUID id, UUID actorId);
    UserResponse uploadAvatar(UUID id, MultipartFile file, UUID actorId);
    byte[] downloadAvatar(UUID id);
    void deleteAvatar(UUID id, UUID actorId);
    void delete(UUID id, UUID actorId);
}
