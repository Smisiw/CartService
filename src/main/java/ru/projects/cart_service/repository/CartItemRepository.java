package ru.projects.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.projects.cart_service.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
