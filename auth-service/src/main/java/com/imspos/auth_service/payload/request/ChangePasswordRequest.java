package com.imspos.auth_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {



    @NotBlank(message = "Old Password can't be empty!")
    private String old_password;

    @NotBlank(message = "New Password can't be empty!")
    private String new_password;

    @NotBlank(message = "Confirm Password can't be empty!")
    private String confirm_password;


}
