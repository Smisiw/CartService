package ru.projects.cart_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.projects.cart_service.dto.CartDto;
import ru.projects.cart_service.dto.CartItemRequestDto;
import ru.projects.cart_service.dto.ValidatedCartItemDto;
import ru.projects.cart_service.service.CartService;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/validate")
    public ResponseEntity<Set<ValidatedCartItemDto>> validateCart(@RequestBody CartDto cartDto) {
        return ResponseEntity.ok(cartService.validateCartItems(cartDto));
    }

    @GetMapping
    public ResponseEntity<Set<ValidatedCartItemDto>> getCart(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeCart(@RequestBody CartDto cartDto, @AuthenticationPrincipal UUID userId) {
        cartService.mergeCarts(cartDto, userId);
        return ResponseEntity.ok("Carts successfully merged");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCartItem(@RequestBody CartItemRequestDto cartItemRequestDto, @AuthenticationPrincipal UUID userId) {
        cartService.addCartItem(cartItemRequestDto.productVariationId(), userId);
        return ResponseEntity.ok("Item successfully added to the cart");
    }

    @PostMapping("/subtract")
    public ResponseEntity<String> subCartItem(@RequestBody CartItemRequestDto cartItemRequestDto, @AuthenticationPrincipal UUID userId) {
        cartService.subtractCartItem(cartItemRequestDto.productVariationId(), userId);
        return ResponseEntity.ok("Item successfully subtracted from the cart");
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearCart(@AuthenticationPrincipal UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared");
    }

}
