package ru.projects.cart_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.projects.cart_service.dto.CartDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;
import ru.projects.cart_service.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/validate")
    public ResponseEntity<List<ValidatedCartItemDto>> validateCart(@RequestBody CartDto cartDto) {
        return ResponseEntity.ok(cartService.validateCartItems(cartDto));
    }

    @GetMapping
    public ResponseEntity<List<ValidatedCartItemDto>> getCart() {
        return ResponseEntity.ok(cartService.getCartItems());
    }

    @PostMapping("/merge")
    public ResponseEntity<List<ValidatedCartItemDto>> mergeCart(@RequestBody CartDto cartDto) {
        return ResponseEntity.ok(cartService.mergeCarts(cartDto));
    }
}
