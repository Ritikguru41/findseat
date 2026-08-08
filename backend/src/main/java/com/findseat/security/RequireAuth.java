package com.findseat.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method as protected.
 * - @RequireAuth → any logged-in user
 * - @RequireAuth(admin = true) → admin only
 * Endpoints without this annotation are public.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    boolean admin() default false;
}
