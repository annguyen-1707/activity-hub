package com.softdreams.activityhub.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import com.softdreams.activityhub.enums.PaymentMethodEnum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    PaymentMethodEnum paymentMethod;
    String shippingAddress;
    String note;

    @NotEmpty
    @Valid
    List<OrderLineRequest> items;
}
