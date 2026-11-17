package com.intecx.inscope.service;

import com.intecx.inscope.dto.response.RoleResponse;
import java.util.List;

public interface RoleService {
    List<RoleResponse> getAll();
    List<RoleResponse> getAllActive();
}
