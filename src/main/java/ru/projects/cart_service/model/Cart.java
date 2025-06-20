package ru.projects.cart_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Cart {
    @Id
    private UUID userId;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CartItem> items = new HashSet<>();

    public Cart(UUID userId) {
        this.userId = userId;
    }

    public void addCartItem(CartItem cartItem) {
        items.removeIf(item -> item.getProductVariationId().equals(cartItem.getProductVariationId()));
        items.add(cartItem);
        cartItem.setCart(this);
    }

    public void addCartItems(Set<CartItem> cartItems) {
        items.removeIf(cartItems::contains);
        items.addAll(cartItems);
        items.forEach(item -> item.setCart(this));
    }

    public void removeCartItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public void removeCartItems(Set<CartItem> cartItems) {
        items.removeAll(cartItems);
        cartItems.forEach(item -> item.setCart(null));
    }

    public void removeAllCartItems() {
        items.forEach(item -> item.setCart(null));
        items.clear();
    }
}
