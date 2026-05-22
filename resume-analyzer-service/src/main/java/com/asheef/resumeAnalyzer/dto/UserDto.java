package com.asheef.resumeAnalyzer.dto;

import lombok.Data;

@Data
public class UserDto {

    private Integer id;
    private String name;
    private String email;
    private String mobile;
    private String address;
    private String role;
}
