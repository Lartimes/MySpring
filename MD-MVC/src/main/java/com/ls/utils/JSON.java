package com.ls.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Lartimes
 * @version 1.0
 * @description: JSON工具类
 * toJSONString
 * parseObject
 * @since 2023/10/24 16:50
 */
public class JSON {
    private JSON() {
    }


    /**
     * toJsonString
     * @param obj
     * @return
     */
    public static String toJSONString(Object obj) {
        if (ClassUtiils.isPrimitiveTypeOrWrapper(obj.getClass())) {
            throw new RuntimeException("the method of toJsonString only support complex Object");
        }
        StringBuilder builder = new StringBuilder();
        Class<?> aClass = obj.getClass();
        builder.append("{");
        Field[] declaredFields = aClass.getDeclaredFields();
        try {
            for (Field field : declaredFields) {
                field.setAccessible(true);
                String key = field.getName();
                Object value = field.get(obj);
                if (value == null) { //优先级最高
                    builder.append("\"" + key + "\" : ");
                    builder.append((String) null);
                    builder.append(",");
                } else if (Number.class.isAssignableFrom(value.getClass())) { //是Number
                    builder.append("\"" + key + "\" : ");
                    builder.append(value);
                    builder.append(",");
                } else if (Boolean.class.isAssignableFrom(value.getClass())) { //如果是booleaan
                    builder.append("\"" + key + "\" : ");
                    builder.append(value);
                    builder.append(",");
                } else if (String.class.isAssignableFrom(value.getClass())) {
                    builder.append("\"" + key + "\" : ");
                    builder.append("\"" + value + "\"");
                    builder.append(",");
                } else if (value.getClass().isArray()) {
                    if (ClassUtiils.isPrimitiveTypeOrWrapper(value.getClass().componentType())) {
                        throw new RuntimeException("the " + field.getName() + "should not be primitiveType arrays");
                    }
                    builder.append("[");
                    for (int i = 0; i < Array.getLength(value); i++) {
                        Object o = Array.get(value, i);
                        builder.append(toJSONString(o));
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("],");
                } else if (!ClassUtiils.isPrimitiveTypeOrWrapper(value.getClass())) {
                    builder.append("\"" + key + "\" : ");
                    builder.append(toJSONString(value));
                    builder.append(",");
                }
            /* * null   "key" : null
            number 优先级 "key" : number
            true /false   "key" : true/false
            string "key" : "value"
            * array     "key" : [递归]
            * object   "key" : {"key..."} 递归
             * */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    public static <T> T parseObject(String jsonStr, Class<T> clazz) {

        if (ClassUtiils.isPrimitiveTypeOrWrapper(clazz)) {
            throw new RuntimeException("the method of toJsonString only support complex Object");
        }
        Field[] fields = clazz.getDeclaredFields();
        T t = null;
        try {
            t = clazz.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Class<?> fieldType = field.getType();
              /* * null   "key" : null 1
            number 优先级 "key" : number
            true /false   "key" : true/false
            string "key" : "value" 2
            * array     "key" : [递归]    3
            * object   "key" : {"key..."} 递归 4
            */
                if (Number.class.isAssignableFrom(fieldType) || Boolean.class.isAssignableFrom(fieldType)) { //是Number或者Boolean
                    int index = jsonStr.indexOf(name);
                    int begin = jsonStr.indexOf(":", index + 1);
                    int end = jsonStr.indexOf(",", begin);
                    if (end == -1) { //---- 说明是最后一个 找到 } trim即可
                        end = jsonStr.indexOf("}", begin);
                    }
                    String value = jsonStr.substring(begin + 1, end).trim();
                    if (!ClassUtiils.primitiveWrapperTypeMap.containsKey(fieldType)) {//包装类
                        fieldType = ClassUtiils.getPrimitiveType(fieldType);
                    }
                    Object invoked = fieldType.getDeclaredMethod("valueOf", String.class).invoke(null, value);
                    field.set(t, invoked);
                    System.out.println(invoked + "注入成功");
                } else if (String.class.isAssignableFrom(fieldType)) {
                    System.out.println(name);
                    int index = jsonStr.indexOf(name);
                    int begin = getStringIndex(jsonStr, index + 1);
                    int end = jsonStr.indexOf("\"", begin);
                    String value = jsonStr.substring(begin, end);
                    field.set(t, value);
                    System.out.println("注入成功" + value);
                    System.out.println("str");
                } else if (fieldType.isArray()) {
//              获取内部基本属性 ， 如果是complex Object 继续否则，error
                    Class<?> componentType = fieldType.componentType();
                    if (ClassUtiils.isPrimitiveTypeOrWrapper(componentType)) {
                        throw new RuntimeException("the " + field.getName() + "should not be primitiveType arrays");
                    }
                    int index = jsonStr.indexOf(name);
                    int begin = jsonStr.indexOf("[", index + 1);
                    int end = getEndIndex(jsonStr.substring(begin));
                    String value = jsonStr.substring(begin, end);
                    Object[] objects = new Object[getArrayLength(value)];
                    for (int i = 0; i < objects.length; i++) {
                        objects[i] = parseObject(getComponentStr(value, i + 1), componentType);
                    }
                    field.set(t, objects);
                    System.out.println("注入成功" + Arrays.toString(objects));
                } else if (!ClassUtiils.isPrimitiveTypeOrWrapper(fieldType)) {
//                    如果是obj
                    int index = jsonStr.indexOf(name);
                    int begin = jsonStr.indexOf("{", index + 1);

                    String value = getObjectEnd(jsonStr.substring(begin));
                    System.out.println(value);
                    field.set(t, parseObject(value, fieldType));
                    System.out.println("诸如成功" + value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(t);
        return t;
    }


    private static String getComponentStr(String value, int sequence) {
        String retStr = null;
        int begin = 0;
        int end = 0;
        for (int i = 0; i < sequence; i++) {
            begin = value.indexOf("{");
            end = value.indexOf("}", begin);
            int countL = 0;
            int countR = 1;
            for (int j = begin; j < end; j++) {
                if (value.charAt(j) == '{') {
                    countL++;
                }
            }
            while (countR != countL) {
                end = value.indexOf("}", end);
                countR++;
            }
            retStr = value.substring(begin, end + 1);
            value = value.substring(end + 1);
        }
        return retStr;
    }

    private static int getEndIndex(String substring) {
        int countA = 0;
        int countB = 0;
        for (int i = 0; i < substring.length(); i++) {
            if (substring.charAt(i) == '[') {
                countA++;
            } else if (substring.charAt(i) == ']') {
                countB++;
            }
            if (countB == countA) {
                return countB + 1;
            }
        }
        throw new RuntimeException("the format of this jsonStr is illegal ");
    }

    /**
     * String类型获取其value “value” 第一个字母的值
     *
     * @param str
     * @return
     */

    private static int getStringIndex(String str, int beginIndex) {
        int index = str.indexOf("\"", str.indexOf("\"", beginIndex) + 1) + 1;
        return index;
    }


    /**
     * @param str
     * @return
     */
    private static int getArrayLength(String str) {
//        { {{}}}, { {{}}} ，{ {{}}} 从中找长度
        int count = 0;
        int begin = 0;
        while ((begin = str.indexOf("{")) != -1) {
            int end = str.indexOf("}");
            while (!countArray(str.substring(begin, end + 1))) {
                end = str.indexOf("}", end + 1);
            }
            count++;
            str = str.substring(end + 1);
        }
        return count;
    }

    private static boolean countArray(String pointCut) {
        int countL = 0;
        int countR = 0;
        for (int i = 0; i < pointCut.length(); i++) {
            if (pointCut.charAt(i) == '{') {
                countL++;
            } else if (pointCut.charAt(i) == '}') {
                countR++;
            }
        }
        return countL == countR;
    }

    private static String getObjectEnd(String str) {
        System.out.println(str);
        int countL = 0;
        int countR = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '{') {
                countL++;
            } else if (str.charAt(i) == '}') {
                countR++;
            }
            if (countL == countR) {
                return str.substring(0, i + 1);
            }
        }
        throw new RuntimeException("the format of this jsonStr is illegal ");
    }
}
