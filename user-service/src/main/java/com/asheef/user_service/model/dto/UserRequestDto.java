package com.asheef.user_service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "Name Should not be blank")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email Format")
    private String email;

    /*
        ^[6-9] - means start digit must be between 6 and 9
        \d - means any digit from 0-9
        {9} - means exact 9 more digit
        \d{9} - The {9} means repeat \d 9 times.
        $ - means end of the string, ➡ No extra characters allowed
     */
    @NotBlank(message = "Mobile Number required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Mobile format")
    private String mobile;

    private String address;

//    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
//    message = "Password must be at least 8 characters long, include uppercase, lowercase, number, and special character"

    @NotBlank(message = "Password should not be empty")
    @Pattern(regexp = "^.{8,}$", message = "Password should be minimum 8 characters long")
    private String password;

    public UserRequestDto(String name, String email, String mobile, String address) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    public UserRequestDto(String name, String email, String mobile, String address, String password) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
        this.password = password;
    }


}
