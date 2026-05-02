package com.asheef.user_service.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PageRequestDto {
    @Min(0)
    private Integer pageNo = 0;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String sortBy = "id";

    @Pattern(regexp = "(?i)asc|desc", message = "direction must be 'asc' or 'desc'")
    private String direction = "asc";
}
