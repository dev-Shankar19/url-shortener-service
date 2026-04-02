package com.e_commerce_oms.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;
    private BigDecimal price;
    private Integer stock;
}