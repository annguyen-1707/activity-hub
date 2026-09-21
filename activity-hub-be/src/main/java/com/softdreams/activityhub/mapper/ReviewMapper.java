package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.softdreams.activityhub.dto.response.ReviewResponse;
import com.softdreams.activityhub.entity.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "orderLineId", source = "orderLine.id")
    @Mapping(target = "productId", source = "orderLine.product.id")
    @Mapping(target = "productName", source = "orderLine.product.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    ReviewResponse toResponse(Review review);
}
