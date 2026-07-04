package com.bbl.devfest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "name must not be blank") String name,
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "email must not be blank") @Email(message = "email must be a valid email address") String email,
        String phone,
        String website) {
}
