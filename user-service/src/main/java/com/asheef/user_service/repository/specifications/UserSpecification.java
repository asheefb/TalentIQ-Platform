package com.asheef.user_service.repository.specifications;

import com.asheef.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<User> getSearch(String search, String fieldName) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()
                    || fieldName == null || fieldName.isBlank()) {
                return cb.conjunction();
            }

            String likePattern = "%" + search.toLowerCase() + "%";

            return cb.like(
                    cb.lower(root.get(fieldName).as(String.class)),
                    likePattern
            );
        };
    }
}
