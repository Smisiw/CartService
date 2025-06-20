package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ValidatedCartItemDto(
        UUID id,
        String name,
        BigDecimal price,
        Integer quantity,
        Integer available,
        Set<AttributeValueDto> attributeValues
) {
}
