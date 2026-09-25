package com.softdreams.activityhub.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.softdreams.activityhub.dto.response.StockTransactionLineResponse;
import com.softdreams.activityhub.dto.response.StockTransactionResponse;
import com.softdreams.activityhub.entity.StockTransaction;
import com.softdreams.activityhub.entity.StockTransactionLine;

@Mapper(componentModel = "spring")
public interface StockTransactionMapper {

    @Mapping(target = "typeLabel", source = "type.label")
    @Mapping(target = "createdByUsername", source = "createdBy.username")
    @Mapping(target = "lines" , ignore = true)
    StockTransactionResponse toResponse(StockTransaction stockTransaction);
    
}
