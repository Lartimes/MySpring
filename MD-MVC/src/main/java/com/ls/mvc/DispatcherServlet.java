package com.ls.mvc;

import com.ls.context.ClassPathApplicationContext;
import com.ls.context.web.WebBeanApplicationContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 16:04
 */
public class DispatcherServlet extends FrameworkServlet {

    private static final String METHOD_GET = "GET";

    private static final String METHOD_POST = "POST";

    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";
    private final ClassPathApplicationContext rawContext;
    private final WebBeanApplicationContext webContext = new WebBeanApplicationContext();
    private final HandlerAdapter handlerAdapter = new HandlerAdapter();
    private String contextConfigLocation;
    private HandlerMapping handlerMapping;

    public DispatcherServlet() {
        super();
        rawContext = (ClassPathApplicationContext) super.getApplicationContexts()[0];
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        contextConfigLocation = config.getInitParameter("contextConfigLocation");
        System.out.println(contextConfigLocation);//classpath:springmvc.xml;classpath:...?
//        获取componentScan[]
        try {
            String[] componentScans = webContext.getComponentScan(contextConfigLocation);
            //        初始化扫描包下所有controller
            webContext.run(getClass(), componentScans);//初始化
            System.out.println(Arrays.toString(componentScans));
            super.getApplicationContexts()[1] = webContext;
            //        进行DI 需要raw原生容器对象
            rawContext.rawContext.forEach((k, v) -> System.out.println(k + " : " + v));
            webContext.dependencyInjection(rawContext); // DI
            webContext.webContext.forEach((k, v) -> System.out.println(k + " : " + v));
//     initHandler  ---》 HandlerMapping
            handlerMapping = new HandlerMapping();// DI --> URI -->
            handlerMapping.initHandler(webContext);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        try {
            Handler handler = handlerMapping.getHandlerByURI(requestURI, "POST");
            Object o = handlerAdapter.actuallyInvoke(handler, request, response);
//            传入class , method 决定是否以流形式返回
//                                  还是只是执行业务逻辑
            System.out.println(o);
            HttpMessageConverter.resolve(response, o, handler);
        } catch (Exception e) {
            response.getWriter().print(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        try {
//
            Handler handler = handlerMapping.getHandlerByURI(requestURI, "GET");
//uri == > handler  获取到Controller Class | methodName | methodType
            Object o = handlerAdapter.actuallyInvoke(handler, request, response);
            System.out.println(o);
            HttpMessageConverter.resolve(response, o, handler);
        } catch (Exception e) {
            response.getWriter().print(e.getMessage());
        }
    }


    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        if (METHOD_GET.equals(method)) {
            long lastModified = getLastModified(request);
            if (lastModified == -1) {
                doGet(request, response);
            } else {
                long ifModifiedSince = request.getDateHeader(HEADER_IFMODSINCE);
                if (ifModifiedSince < lastModified) {
                    maybeSetLastModified(response, lastModified);
                    doGet(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            }

        } else if (method.equals(METHOD_POST)) {
            doPost(request, response);
        }

    }

    private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD)) {
            return;
        }
        if (lastModified >= 0) {
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
        }
    }
}
