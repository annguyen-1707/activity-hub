package com.softdreams.activityhub.service;

import java.util.HashSet;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.constant.PredefinedRole;
import com.softdreams.activityhub.dto.request.UserCreationRequest;
import com.softdreams.activityhub.dto.request.UserUpdateRequest;
import com.softdreams.activityhub.dto.response.UserResponse;
import com.softdreams.activityhub.entity.Role;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.UserMapper;
import com.softdreams.activityhub.repository.RoleRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isUserOwner(#userId, authentication)")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        log.info(
                "Password is null ? {}",
                request.getPassword() != null && !request.getPassword().isBlank());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                            || auth.getAuthority().equals("ADMIN"));
            if (!isAdmin) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isUserOwner(#userId, authentication)")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsers(Pageable pageable, String keyword) {
        log.info("In method get Users");
        return userRepository.searchUserByKeyword(pageable, keyword).map(userMapper::toUserResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isUserOwner(#userId, authentication)")
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
