package com.test.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/22 14:50
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Student {
    private Integer id;
    private String name;
    private Clazz clazz;


}
