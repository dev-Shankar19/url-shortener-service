package com.e_commerce_oms.dto.response;

import com.e_commerce_oms.entity.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private PaymentStatus status;
}