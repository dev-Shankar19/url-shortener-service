package com.e_commerce_oms.repository;

import com.e_commerce_oms.entity.Payment;
import com.e_commerce_oms.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long>{
    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndRetryCountLessThan(PaymentStatus status,int retryLimit);
}
