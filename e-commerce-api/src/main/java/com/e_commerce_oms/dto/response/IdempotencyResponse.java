package com.e_commerce_oms.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyResponse {

    private String key;
    private String response;
}