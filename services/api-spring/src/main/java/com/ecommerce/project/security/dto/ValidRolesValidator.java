package com.ecommerce.project.security.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class ValidRolesValidator implements ConstraintValidator<ValidRoles, Set<String>> {

    private static final Set<String> ALLOWED = Set.of("user", "seller");

    @Override
    public boolean isValid(Set<String> roles, ConstraintValidatorContext context) {
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        // All provided values must be within the allowed set; at least one must match
        return !roles.isEmpty() && ALLOWED.containsAll(roles);
    }
}
