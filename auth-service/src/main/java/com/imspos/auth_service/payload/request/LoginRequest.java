package com.imspos.auth_service.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email can't be empty!")
    @Email(message = "Email Address should be valid!")
    private String email;

    @NotBlank(message = "Password can't be empty!")
    private String password;

}
