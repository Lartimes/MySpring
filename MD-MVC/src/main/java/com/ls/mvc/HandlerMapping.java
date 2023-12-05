package com.ls.mvc;

import com.ls.context.web.WebBeanApplicationContext;
import com.ls.stereotype.web.GetMapping;
import com.ls.stereotype.web.PostMapping;
import com.ls.stereotype.web.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 16:07
 */
public class HandlerMapping {
    /*
    * 采用 HashMap<URI , HashSet<Handler> >
    (URI+ method) primaryKey
    *   private Object obj;
    private String methodType;
    private String methodName;
    * */

    private final HashMap<String, HashSet<Handler>> uriPatterns = new HashMap<>();

    public void initHandler(WebBeanApplicationContext webContext) {
        webContext.webContext.forEach((beanId, clazz) -> {
            Class<?> aClass = clazz.getClass();
            String basePath = null;
            if (aClass.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
                basePath = annotation.value()[0];
            }
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
//                转化为Handler对象
                Map<String, HashSet<Handler>> handler = getHandler(clazz, method, basePath);
//                key 决定是否合并 或者 直接存放
                if (handler == null) {
                    continue;
                }
                for (String s1 : handler.keySet()) {
                    if (uriPatterns.containsKey(s1)) {
                        HashSet<Handler> handlers = uriPatterns.get(s1);
                        handlers.addAll(handler.get(s1));
                        uriPatterns.put(s1, handlers);
                    } else {
                        uriPatterns.put(s1, handler.get(s1));
                    }
                }
            }
            uriPatterns.forEach((k, v) -> {
                System.out.println("\t\t" + k);
                v.forEach(System.out::println);
            });
//初始化mapping完毕
        });
    }

    private Map<String, HashSet<Handler>> getHandler(Object clazz, Method method, String basePath) {
        HashMap<String, HashSet<Handler>> retMaps = new HashMap<>();
        String[] value = null;
        String methodType = null;
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            value = annotation.value();
            methodType = annotation.method();
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            value = annotation.value();
            methodType = "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            value = annotation.value();
            methodType = "POST";
        }
        if (value == null) {
            return null;
        }
        /*
        进行合并或者直接put
        * */
        for (String temp : value) {
            String key = basePath == null ? temp : basePath + temp;
            Handler handler = new Handler(method.getParameterTypes(), clazz, methodType, method.getName());
//            parameters
            HashSet<Handler> handlers = null;
            if (retMaps.containsKey(key)) {
                handlers = retMaps.get(key);
            } else {
                handlers = new HashSet<>();
            }
            handlers.add(handler);
            retMaps.put(key, handlers);
        }
        return retMaps;
    }

    /**
     * 根据URI获取对应的Handler
     *
     * @param requestURI
     * @return
     */
    public Handler getHandlerByURI(String requestURI, String methodType) throws Exception {
        System.out.println(requestURI);
        HashSet<Handler> handlers = uriPatterns.get(requestURI);
        if (handlers == null) {
            throw new Exception("there hasn't the request method that matches it");
        }
        if (handlers.size() > 2) {
            throw new Exception("the uri only matches one method type");
        }
        List<Handler> list = handlers.stream().filter((handler -> handler.getMethodType().equals(methodType))).toList();
        if (list.size() == 0) {
            throw new Exception("there hasn't the request method that matches it");
        }
        return list.get(0);
    }
}
