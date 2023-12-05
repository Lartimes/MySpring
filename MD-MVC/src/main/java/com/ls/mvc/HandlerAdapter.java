package com.ls.mvc;

import com.ls.stereotype.web.RequestBody;
import com.ls.stereotype.web.RequestParam;
import com.ls.utils.ClassUtiils;
import com.ls.utils.HttpGetJson;
import com.ls.utils.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 16:07
 */
public class HandlerAdapter {
    /*
     *  RestController 优先级高于ResponseBody
     * ResponseBody
     *
     * */
    public Object actuallyInvoke(Handler handler, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object invoke = null;
        Object obj = handler.getObj();
        String methodName = handler.getMethodName();
        Class<?> aClass = obj.getClass();
        Method declaredMethod = aClass.getDeclaredMethod(methodName, handler.getParameters());
        int length = handler.getParameters().length;
        if (length == 0) {
            invoke = declaredMethod.invoke(obj);
            return invoke;
        }
//        进行请求映射 注入
        Object[] objects = requestInjection(request, declaredMethod, length);
        invoke = declaredMethod.invoke(obj, objects);
        System.out.println(invoke+"invoke");
        return invoke;
    }

    /*
    * //RequestBody
//RequestParam
    * */
    private Object[] requestInjection(HttpServletRequest request, Method declaredMethod, int length) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] objects = new Object[length];
        Map<String, String[]> parameterMap = request.getParameterMap();
        Parameter[] parameters = declaredMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam annotation = parameter.getAnnotation(RequestParam.class);
                String key = annotation.value();
                String value = parameterMap.get(key)[0];
//                name=value
                if (!ClassUtiils.primitiveWrapperTypeMap.containsKey(type)) {
                    type = ClassUtiils.getPrimitiveType(type);
                }
                if(String.class.isAssignableFrom(type)){
                    objects[i] = value;
                    continue;
                }
                Object invoke = type.getDeclaredMethod("valueOf", String.class).invoke(null, value);
                objects[i] = invoke;
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
//                读取json数据
//                contentType = “application/json”
                String json = HttpGetJson.getJson(request);
                System.out.println(json);
                objects[i] = JSON.parseObject(json, type);//
            } else {
//--->如果是一个参数 直接赋值
//                否则 报错
                if (parameterMap.size() == 1) {
                    if (!ClassUtiils.primitiveWrapperTypeMap.containsKey(type)) {
                        type = ClassUtiils.getPrimitiveType(type);
                    }
                    String value = parameterMap.values().stream().findFirst().get()[0];
                    if(String.class.isAssignableFrom(type)){
                        objects[i] = value;
                        continue;
                    }
                    Object invoke = type.getDeclaredMethod("valueOf", String.class).invoke(null, value);
                    objects[i] = invoke;
                } else {
                    throw new RuntimeException("the number of arguments should be only one");
                }
            }
        }
        return objects;
    }

}
