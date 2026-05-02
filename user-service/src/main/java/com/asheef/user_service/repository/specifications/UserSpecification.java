package com.asheef.user_service.repository.specifications;

import com.asheef.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public final class UserSpecification {

    private static final Set<String> SEARCHABLE_FIELDS =
            Set.of("name", "email", "mobile", "address");

    private UserSpecification() {}

    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? cb.conjunction() : cb.equal(root.get("isActive"), active);
    }

    public static Specification<User> search(String search, String fieldName) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank() || fieldName == null || fieldName.isBlank()) {
                return cb.conjunction();
            }
            if (!SEARCHABLE_FIELDS.contains(fieldName)) {
                // Ignore invalid field silently rather than 500-ing.
                return cb.conjunction();
            }
            String likePattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get(fieldName).as(String.class)), likePattern);
        };
    }
}
