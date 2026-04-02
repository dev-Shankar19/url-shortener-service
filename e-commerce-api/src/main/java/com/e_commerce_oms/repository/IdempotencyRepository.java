package com.e_commerce_oms.repository;

import com.e_commerce_oms.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String>{
}

