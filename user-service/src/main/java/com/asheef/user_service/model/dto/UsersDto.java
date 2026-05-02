package com.asheef.user_service.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UsersDto extends PageRequestDto {
    private Boolean isActive;
    private String search;
    private String fieldName;
}
