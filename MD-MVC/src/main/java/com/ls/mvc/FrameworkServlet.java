package com.ls.mvc;

import com.ls.context.ApplicationContext;
import com.ls.stereotype.SpringBootApplication;
import jakarta.servlet.http.HttpServlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/23 21:30
 */
public class FrameworkServlet extends HttpServlet {
    private ApplicationContext[] applicationContexts;
    {
        applicationContexts = new ApplicationContext[2];
    }

    public FrameworkServlet() {
        // 加载原生容器
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path);
        try {
            Class<Object> bootStarterClass = getBootStarterClass(file, file.getCanonicalPath());
            if(bootStarterClass == null){
                throw new RuntimeException("the classes of this project don't have the SpringbootApplication annotated");
            }
            Object obj = bootStarterClass.getDeclaredConstructor().newInstance();
            Method start = bootStarterClass.getDeclaredMethod("start");
            applicationContexts[0] = (ApplicationContext) start.invoke(obj);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public FrameworkServlet(ApplicationContext[] applicationContexts) {
        this.applicationContexts = applicationContexts;
    }

    /**
     * @param file     目录
     * @param basePath 项目src根路径
     */
    private Class<Object> getBootStarterClass(File file, String basePath) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isFile() && listFile.getName().endsWith(".class")) {
                String path = listFile.getPath();
                String clazzName = path.substring(basePath.length() + 1, path.length() -
                        ".class".length()).replaceAll("\\\\", ".");
                Class<?> aClass = Class.forName(clazzName);
                if (aClass.isAnnotationPresent(SpringBootApplication.class)) {
                    System.out.println(clazzName);
                    return (Class<Object>) aClass;
                }
                return null;
            } else if (listFile.isDirectory()) {
                Class<Object> clazz = getBootStarterClass(listFile, basePath);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        return null;
    }

    public ApplicationContext[] getApplicationContexts() {
        return applicationContexts;
    }

    public void setApplicationContexts(ApplicationContext[] applicationContexts) {
        this.applicationContexts = applicationContexts;
    }


}
