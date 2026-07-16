package com.example.bookkeeping.server.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}
