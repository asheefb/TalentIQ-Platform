package com.asheef.user_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Integer id;
    private String name;
    private String email;
    private String mobile;
    private String address;
    private Boolean isActive;
}
