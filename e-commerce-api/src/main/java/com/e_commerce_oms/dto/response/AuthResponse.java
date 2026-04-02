package com.e_commerce_oms.dto.response;

import com.e_commerce_oms.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Role role;
}