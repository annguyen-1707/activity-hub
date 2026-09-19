package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;

public interface OrderLineProjection {

    String getId();

    String getOrderId();

    String getProductId();

    String getProductName();

    Integer getQuantity();

    BigDecimal getUnitPrice();

    BigDecimal getSubtotal();
}