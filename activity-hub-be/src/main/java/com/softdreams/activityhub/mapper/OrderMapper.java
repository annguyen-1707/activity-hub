package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderLineResponse;
import com.softdreams.activityhub.dto.response.OrderResponse;
import com.softdreams.activityhub.entity.Order;
import com.softdreams.activityhub.entity.OrderLine;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Order toOrder(OrderRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "orderLines")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderLineResponse toOrderLineResponse(OrderLine orderLine);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateOrder(@MappingTarget Order order, OrderRequest request);
}
