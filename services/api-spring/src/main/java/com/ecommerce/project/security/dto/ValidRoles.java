package com.ecommerce.project.security.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidRolesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoles {
    String message() default "Each role must be one of: user, seller";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
