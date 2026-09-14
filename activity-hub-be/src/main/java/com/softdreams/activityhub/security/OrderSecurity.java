package com.softdreams.activityhub.security;

import com.softdreams.activityhub.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean isOwner(String orderId, Authentication authentication) {

        String userId = authentication.getName();

        return orderRepository.existsByIdAndUserId(orderId, userId);
    }
}
