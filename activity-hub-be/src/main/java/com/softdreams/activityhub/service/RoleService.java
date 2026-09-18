package com.softdreams.activityhub.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.RoleRequest;
import com.softdreams.activityhub.dto.response.RoleResponse;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.RoleMapper;
import com.softdreams.activityhub.repository.RoleRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    UserRepository userRepository;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        if (!roleRepository.existsById(role)) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }

        if (userRepository.existsByRoles_Name(role)) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        roleRepository.deleteById(role);
    }
}
