package com.softdreams.activityhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.softdreams.activityhub.repository.OrderRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component("security")
@RequiredArgsConstructor
public class SecurityService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public boolean isOrderOwner(String orderId, Authentication authentication) {
        String userId = authentication.getName();
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    public boolean isUserOwner(String userId, Authentication authentication) {
        String userIdAuth = authentication.getName();
        return userIdAuth.equals(userId);
    }
}
