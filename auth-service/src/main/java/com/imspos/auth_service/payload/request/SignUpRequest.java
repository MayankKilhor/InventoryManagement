package com.imspos.auth_service.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "Name can't be empty!")
    @Size(max = 100, message = "Name must be at most 100 characters!")
    private String name;

    @NotBlank(message = "Phone No. can't be empty!")
    @Size(max = 15, message = "Phone Number must be at most 15 characters!")
    @Pattern(
            regexp = "^\\+\\d{1,3}-\\d{7,12}$",
            message = "Phone Number must be in the format +<1-3 digits>-<7-12 digits> (e.g., +91-9876543210)"
    )
    private String phoneNo;

    private String countryCode;

    @NotBlank(message = "Email can't be empty!")
    @Email(message = "Email address should be valid!")
    @Size(max = 100, message = "Email must be at most 100 characters!")
    private String email;

    @NotBlank(message = "Password can't be empty!")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters!")
    private String password;

    @NotBlank(message = "Confirm Password can't be empty!")
    @Size(min = 6, max = 50, message = "Confirm Password must be between 6 and 50 characters!")
    private String confirm_password;

    private Boolean selectedPermission;


}
