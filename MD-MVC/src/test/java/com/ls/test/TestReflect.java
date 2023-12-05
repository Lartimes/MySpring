package com.ls.test;

import com.ls.utils.JSON;
import com.test.bean.Clazz;
import com.test.bean.Student;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/24 11:10
 */
public class TestReflect {


    @Test
    public void test01() throws Exception {
        Class<?> aClass = Class.forName("com.test.controller.UserController");
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Class<?> type = parameter.getType();
                System.out.println(type);
            }
        }
    }

    @Test
    public void test02() {
        Student student = new Student(1, "nan", new Clazz(1, "大数据222"));
        String jsonString = JSON.toJSONString(student);
        Student student1 = JSON.parseObject(jsonString, Student.class);
        System.out.println(student1);
        System.out.println(jsonString);
    }


    @Test
    public void test03() {
        System.out.println(Number.class.isAssignableFrom(Integer.class));
        System.out.println(Object[].class.isArray());
        Object o = true;
        System.out.println(Boolean.class.isAssignableFrom(o.getClass()));
        System.out.println(o.getClass());
        Object s = "erg";
        System.out.println(String.class.isAssignableFrom(s.getClass()));
        System.out.println((String) null);

        Integer[] integers = new Integer[2];
        System.out.println(integers.getClass());
        System.out.println(Integer.class);
        System.out.println(integers.getClass().getComponentType());
    }

    @Test
    public void test05() {
        Student student = new Student(1, "reg", new Clazz(1, "ewgwe"));
        String jsonString = com.ls.utils.JSON.toJSONString(student);
        System.out.println(jsonString);
        Student student1 = com.ls.utils.JSON.parseObject(jsonString, Student.class);
        System.out.println(student1);

    }

    @Test
    public void test06() {
        String str = "ergerg";
        System.out.println(str.substring(0, str.length() - 1));
    }

    @Test
    public void test07() throws Exception {
        Class<?> aClass = Class.forName("com.test.controller.UserController");
        for (Method declaredMethod : aClass.getDeclaredMethods()) {
            Parameter[] parameters = declaredMethod.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println(parameter.getType());
            }
        }
    }
}
