package ru.projects.cart_service.dto;

import java.util.UUID;

public record CartItemRequestDto(
        UUID productVariationId
) {
}
