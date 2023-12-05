package com.ls.mvc;

import com.ls.stereotype.web.ResponseBody;
import com.ls.stereotype.web.RestController;
import com.ls.utils.ClassUtiils;
import com.ls.utils.JSON;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 16:07
 */
public class HttpMessageConverter {

    public static void resolve(HttpServletResponse response, Object o, Handler handler) {
        try {
            Class<?> aClass = handler.getObj().getClass();
//        如果是RestController 根据返回类型返回  ---优先级最高
            Method declaredMethod = aClass.getDeclaredMethod(handler.getMethodName(), handler.getParameters());
            if (aClass.isAnnotationPresent(RestController.class)) {
                System.out.println("restController");
                retObj(declaredMethod, response, o);
            } else if (declaredMethod.isAnnotationPresent(ResponseBody.class)) {
//        看是否是responseBody注解了
//        注解根据类型返回
                retObj(declaredMethod, response, o);
            }
//        其他的的话不做处理
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void retObj(Method declaredMethod, HttpServletResponse response,
                               Object o) throws Exception {
        response.setCharacterEncoding("UTF-8");
        if (ClassUtiils.isPrimitiveTypeOrWrapper(declaredMethod.getReturnType())) {
            response.setContentType("html/text;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print(o);
            return;
        }
        response.setContentType("html/json");
        PrintWriter out = response.getWriter();
        out.print(JSON.toJSONString(o));
    }
}
