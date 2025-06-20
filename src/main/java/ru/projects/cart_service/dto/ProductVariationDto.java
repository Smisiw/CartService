package ru.projects.cart_service.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ProductVariationDto(
        UUID id,
        String name,
        BigDecimal price,
        int quantity,
        int reserved,
        Set<AttributeValueDto> attributeValues
) {
}
