package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        Set<Item> items,
        BigDecimal totalPrice,
        String paymentMethod,
        String deliveryAddress
) {
    public record Item(
            Long id,
            Long productVariationId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}