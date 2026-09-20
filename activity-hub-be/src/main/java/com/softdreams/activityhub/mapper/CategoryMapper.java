package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.softdreams.activityhub.dto.request.CategoryRequest;
import com.softdreams.activityhub.dto.response.CategoryResponse;
import com.softdreams.activityhub.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toCategory(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);

    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toCategoryResponse(Category category);
}
