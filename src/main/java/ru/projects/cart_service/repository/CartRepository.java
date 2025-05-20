package ru.projects.cart_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.projects.cart_service.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}
