package com.intecx.stride.service;

import com.intecx.stride.dto.response.RoleResponse;
import java.util.List;

public interface RoleService {
    List<RoleResponse> getAll();
    List<RoleResponse> getAllActive();
}
