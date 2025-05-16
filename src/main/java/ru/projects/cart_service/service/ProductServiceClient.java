package ru.projects.cart_service.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.projects.cart_service.dto.ProductVariationDto;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products")
public interface ProductServiceClient {

    @PostMapping("/variations-by-ids")
    List<ProductVariationDto> getVariationsByIds(@RequestBody List<Long> productIds);
}
