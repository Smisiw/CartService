package ru.projects.cart_service.dto;

import java.util.UUID;

public record CartItemDto(
        UUID productVariationId,
        int quantity
) {
}
