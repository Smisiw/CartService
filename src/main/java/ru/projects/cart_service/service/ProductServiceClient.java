package ru.projects.cart_service.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.projects.cart_service.dto.ProductVariationDto;

import java.util.Set;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products")
public interface ProductServiceClient {

    @PostMapping("/variationsByIds")
    ResponseEntity<Set<ProductVariationDto>> getVariationsByIds(@RequestBody Set<Long> productIds);
}
