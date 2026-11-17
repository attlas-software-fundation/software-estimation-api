package com.intecx.inscope.service.impl;

import com.intecx.inscope.dto.response.RoleResponse;
import com.intecx.inscope.mapper.RoleMapper;
import com.intecx.inscope.repository.RoleRepository;
import com.intecx.inscope.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllActive() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(roleMapper::toResponse)
                .toList();
    }
}
