package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ValidatedCartItemDto(
        Long id,
        String name,
        BigDecimal price,
        Integer quantity,
        Integer available,
        Set<AttributeValueDto> attributeValues
) {
}
