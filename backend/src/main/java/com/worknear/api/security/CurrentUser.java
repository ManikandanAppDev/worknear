package com.worknear.api.security;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated {@link UserPrincipal} into a controller method.
 * Hidden from Swagger since it is derived from the bearer token, not a request param.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
@Parameter(hidden = true, in = ParameterIn.HEADER)
public @interface CurrentUser {
}
