package com.ls.bootStarter;

import com.ls.context.ApplicationContext;
import com.ls.context.ClassPathApplicationContext;
import com.ls.context.web.WebBeanApplicationContext;
import com.ls.stereotype.ComponentScan;
import com.ls.stereotype.SpringBootApplication;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;


/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/20 22:48
 */
public class MVCBootApplication {

    private static String[] basePackages = new String[]{""};
    private final ApplicationContext webBeanContext = new WebBeanApplicationContext();
    private ApplicationContext classPathContext;

    public static ApplicationContext run(Class clazz) {
        try {
            MVCBootApplication mvcBootApplication = new MVCBootApplication();
            mvcBootApplication.classPathContext = new ClassPathApplicationContext();
            mvcBootApplication.initBean(clazz);
            return mvcBootApplication.classPathContext;
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ApplicationContext run(Class clazz, String... args) throws IOException, NoSuchFieldException, IllegalAccessException {
        MVCBootApplication mvcBootApplication = new MVCBootApplication();
        mvcBootApplication.classPathContext =
                new ClassPathApplicationContext(args);
        try {
            mvcBootApplication.initBean(clazz);
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
        return mvcBootApplication.classPathContext;
    }

    private void initBean(Class clazz) throws NoSuchFieldException, IllegalAccessException, IOException {
        if (!clazz.isAnnotationPresent(SpringBootApplication.class)) {
            throw new RuntimeException(
                    "please mark it as starter class used SpringBootApplication");
        }
        Annotation[] annotations = clazz.getAnnotations();
        List<Annotation> list = Arrays.stream(annotations).
                filter((anno) -> anno instanceof ComponentScan).toList();
        if (list.size() != 0) {
            ComponentScan anno = (ComponentScan) list.get(0);
            basePackages = anno.basePackages();
//            System.out.println(Arrays.toString(basePackages));
        }

//        检测springbootApplication 注解 和 componentScan 注解
//        web 容器 和 原生容器两个容器 ， 三层架构的原因只加载 除controller注解之外的
        classPathContext.run(clazz, basePackages);

    }

}
