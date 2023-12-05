package com.ls.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/20 22:18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Repository {
    @AliasFor(
            annotation =  Component.class
    )
    String value() default "";
}
