package com.ls.stereotype;

import java.lang.annotation.*;

/**
 * @author Lartimes
 * @version 1.0
 * @description: 为其他注解起别名
 * @since 2023/10/20 22:22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AliasFor {
    Class<? extends Annotation>  annotation() default Annotation.class;
}
