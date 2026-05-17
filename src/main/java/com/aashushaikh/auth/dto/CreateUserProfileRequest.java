package com.aashushaikh.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserProfileRequest {
    private String id;
    private String username;
    private String email;
}
