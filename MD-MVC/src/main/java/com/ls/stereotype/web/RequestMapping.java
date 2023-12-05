package com.ls.stereotype.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 15:58
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String[] value() default {};

    String method() default "GET";
}
