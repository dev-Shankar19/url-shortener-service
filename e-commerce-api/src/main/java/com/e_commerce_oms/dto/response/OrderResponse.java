package com.e_commerce_oms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse{
    private Long orderId;
    private String status;
    private double totalAmount;
    private List<OrderItemDTO> items;
}
