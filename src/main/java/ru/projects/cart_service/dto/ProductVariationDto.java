package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProductVariationDto(
        Long id,
        String name,
        BigDecimal price,
        int quantity,
        int reserved,
        Set<AttributeValueDto> attributeValues
) {
}
