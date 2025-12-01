package com.pm.authservice.dto;

// this class is used to send data back to the client after login is successful.
public class LoginResponseDTO {
    private final String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
