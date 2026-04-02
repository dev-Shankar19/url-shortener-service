package com.e_commerce_oms.repository;

import com.e_commerce_oms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>{
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
