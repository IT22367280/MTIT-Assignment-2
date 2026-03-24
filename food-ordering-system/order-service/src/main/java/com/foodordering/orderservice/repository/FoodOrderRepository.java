package com.foodordering.orderservice.repository;

import com.foodordering.orderservice.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
}
