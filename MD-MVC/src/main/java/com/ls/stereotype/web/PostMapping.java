package com.ls.stereotype.web;

import com.ls.stereotype.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 15:59
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface PostMapping {
    @AliasFor(
            annotation =  RequestMapping.class
    )
    String[] value() default{};
}
