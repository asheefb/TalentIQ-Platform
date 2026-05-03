package com.asheef.user_service.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class PageRequestDto {

    /**
     * Allowlist of fields a client is allowed to sort by.
     * Any other value is coerced to "id" to avoid PropertyReferenceException /
     * leaking internal column names.
     */
    public static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "name", "email", "mobile", "createdAt", "updatedAt");

    @Min(0)
    private Integer pageNo = 0;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String sortBy = "id";

    @Pattern(regexp = "(?i)asc|desc", message = "direction must be 'asc' or 'desc'")
    private String direction = "asc";

    /** Returns sortBy if whitelisted, else "id". */
    public String safeSortBy() {
        return ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";
    }
}