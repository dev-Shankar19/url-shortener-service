package com.e_commerce_oms.repository;

import com.e_commerce_oms.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("""
        UPDATE Product p
        SET p.stock = p.stock - :qty
        WHERE p.id = :id AND p.stock >= :qty
    """)
    int reduceStock(@Param("id") Long id,
                    @Param("qty") int qty);
}