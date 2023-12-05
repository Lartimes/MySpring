package com.ls.utils;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lartimes
 * @version 1.0
 * @description: ClassUtils 工具类
 * 进行判断是否基本
 * 基本类型 -->
 * 基本类型的话 -->
 * @since 2023/10/22 14:00
 */
public class ClassUtiils {
    public static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(9);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
        primitiveWrapperTypeMap.put(String.class, String.class);
    }

    private ClassUtiils() {
    }

    /**
     * 判断是否是基本类型 或者包装类
     * @param clazz
     * @return
     */
    public static boolean isPrimitiveTypeOrWrapper(Class<?> clazz) {

        return primitiveWrapperTypeMap.containsKey(clazz) ||
                primitiveWrapperTypeMap.containsValue(clazz);
    }


    /**
     * 根据int 这种简单类型获取其包装类型
     * @param clazz
     * @return
     */
    public static Class<?> getPrimitiveType(Class<?> clazz) {
        AtomicReference<Class> retClazz = new AtomicReference<>(clazz);
        if (primitiveWrapperTypeMap.containsValue(clazz)) {
            primitiveWrapperTypeMap.forEach((k ,v)->{
                if(v.equals(clazz)){
                    retClazz.set(k);
                }
            });
        }
        return retClazz.get();
    }
}
