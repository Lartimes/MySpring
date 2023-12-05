package com.ls.context;

import java.io.IOException;

/**
 * @author Lartimes
 * @version 1.0
 * @description: Bean顶级容器
 * @since 2023/10/20 22:44
 */
public interface ApplicationContext {

    String getApplicationName();


    void run(Class clazz, String[] basePackages) throws IOException;


    Object getBean(String beanId);

    <T> T getBean(String name, Class<T> requiredType);


}
