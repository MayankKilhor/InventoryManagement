package com.imspos.auth_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDetailsRequest {


    @NotBlank(message = "Name can't be empty!")
    @Size(max = 25, message = "Name must be at most 25 characters!")
    private String name;


    @Size(max = 25, message = "Designation must be at most 25 characters!")
    private String designation;


    @Size(max = 25, message = "Location must be at most 25 characters!")
    private String location;

    @NotBlank(message = "Phone Number can't be empty!")
    @Size(max = 15, message = "Phone Number must be at most 15 characters!")
    @Pattern(
            regexp = "^\\+\\d{1,3}-\\d{7,12}$",
            message = "Phone Number must be in the format +<1-3 digits>-<7-12 digits> (e.g., +91-9876543210)"
    )
    private String phoneNo;

}
