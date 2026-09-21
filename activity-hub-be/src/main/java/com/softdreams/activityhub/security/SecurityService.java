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
        return userRepository
                .findByUsername(authentication.getName())
                .map(user -> orderRepository.existsByIdAndUserId(orderId, user.getId()))
                .orElse(false);
    }

    public boolean isUserOwner(String userId, Authentication authentication) {
        return userRepository
                .findByUsername(authentication.getName())
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
