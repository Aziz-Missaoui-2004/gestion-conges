package com.cni.gestionconges.dto;

import com.cni.gestionconges.entity.Role;

public class LoginResponse {

    private Long userId;
    private String email;
    private Role role;
    private String token;

    public LoginResponse(
            Long userId,
            String email,
            Role role,
            String token) {

        this.userId = userId;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}