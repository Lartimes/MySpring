package com.ls.context.web;

import com.ls.cons.IoCConstants;
import com.ls.context.ApplicationContext;
import com.ls.context.ClassPathApplicationContext;
import com.ls.stereotype.Autowired;
import com.ls.stereotype.Qualifier;
import com.ls.stereotype.Resource;
import com.ls.stereotype.web.Controller;
import com.ls.stereotype.web.RestController;
import com.ls.utils.Resources;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/21 11:07
 */
public class WebBeanApplicationContext implements ApplicationContext {


    public Map<String, Object> webContext = new HashMap<>();
    private String basePackage;

    @Override
    public String getApplicationName() {
        return IoCConstants.WEB_CONTEXT_NAME;
    }

    public String[] getComponentScan(String contextConfigLocation) throws DocumentException {
        //classpath:springmvc.xml;classpath:...?
        String[] strings = contextConfigLocation.split(",");
        String[] xmlName = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            int index = strings[i].indexOf(",");
            if (index == -1) {
                xmlName[i] = strings[i].substring("classpath:".length());
            } else {
                xmlName[i] = strings[i].substring("classpath:".length(), index);
            }
        }
        HashMap<String, Object> componentScans = new HashMap<>();
        SAXReader saxReader = new SAXReader();
        for (String name : xmlName) {
            System.out.println(name);
            Document document = saxReader.read(Resources.getResourcesAsStream(name));
            List list = document.selectNodes("/beans/component-scan");
            list.forEach((node) -> {
                Element element = (Element) node;
                String basePackage = element.attributeValue("base-package");
                if (basePackage.contains(",")) {
                    String[] paths = basePackage.split(",");
                    for (String path : paths) {
                        componentScans.put(path, null);
                    }
                } else {
                    componentScans.put(basePackage, null);
                }
            });
        }
        return componentScans.keySet().toArray(new String[0]);

    }

    @Override
    public void run(Class clazz, String[] basePackages) {
        for (String basePackage : basePackages) {
            this.basePackage = basePackage; //class.forname 相对路径解析
            String path = Objects.requireNonNull(clazz.getClassLoader().getResource(basePackage.replaceAll("\\.", "/"))).getPath();
            File file = new File(path);
            List<File> list = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter((fl) -> !file.isFile() || file.getName().endsWith(".class")).toList();
            System.out.println(list);
            //file + directory
            initBeanInstance(list);
        }
        webContext.forEach((k, v) -> System.out.println(k + " : " + v));
    }


    public void dependencyInjection(ClassPathApplicationContext applicationContext) { //----只对注解类进行DI
        /*
        1. Autowired type
        2.Resource name
        3.set注入 type
        优先级如上
        * */
        webContext.forEach((k, v) -> {
            Class<?> aClass = v.getClass();
            System.out.println(aClass.getName());
            if (!Objects.isNull(isAnnotationed(aClass))) {
                Field[] fields = aClass.getDeclaredFields();
                for (Field field : fields) {
                    Map<String, String> diPattern = getDIPattern(field);
                    String pattern = (String) diPattern.keySet().toArray()[0];
                    String beanId = diPattern.get(pattern);
                    System.out.println(beanId);
                    Class<?> fieldType = field.getType();
//                    resource qualifier  autowired set
                    if ("resource".equals(pattern) || "qualifier".equals(pattern)) { //--- byName
                        applicationContext.rawContext.forEach((id, clazz) -> {
                            if (id.equals(beanId)) {
                                field.setAccessible(true);
                                try {
                                    field.set(v, clazz);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } else if ("autowired".equals(pattern)) { // byType
                        List<Object> list = applicationContext.rawContext.values().stream().filter((clazz) -> {
                            if(fieldType.isInterface()){//如果是接口应该筛选实现接口为他的所有集合
                                boolean flag = false;
                                for (Class<?> anInterface : clazz.getClass().getInterfaces()) {
                                    if(fieldType.equals(anInterface)){
                                        flag = true;
                                    }
                                }
                                return flag;
                            }
                            return  clazz.getClass().equals(fieldType);
                        }).toList();
                        if (list.size() != 1) {
                            throw new RuntimeException("the number of  " + aClass + " class should be  only has one ");
                        }
                        try {
                            field.setAccessible(true);
                            field.set(v, list.get(0));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    } else if ("set".equals(pattern)) { //-- byType
                        List<Object> list = applicationContext.rawContext.values().stream().filter((clazz) -> {
                            if(fieldType.isInterface()){//如果是接口应该筛选实现接口为他的所有集合
                                boolean flag = false;
                                for (Class<?> anInterface : clazz.getClass().getInterfaces()) {
                                    if(fieldType.equals(anInterface)){
                                        flag = true;
                                    }
                                }
                                return flag;
                            }
                            return  clazz.getClass().equals(fieldType);
                        }).toList();
                        if (list.size() != 1) {
                            throw new RuntimeException("the number of  " + aClass + " class should be  only has one ");
                        }
                        String name = field.getName();
                        String methodName = "set" + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
                        try {
                            Method method = aClass.getDeclaredMethod(methodName, field.getType());
                            method.invoke(v, list.get(0));
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });

    }

    private Map<String, String> getDIPattern(Field field) {
        if (field.isAnnotationPresent(Resource.class)) {
            Resource annotation = field.getAnnotation(Resource.class);
            return Map.of("resource", annotation.name());
        } else if (field.isAnnotationPresent(Autowired.class)) {
            Autowired annotation = field.getAnnotation(Autowired.class);
            Annotation[] annotations = field.getAnnotations();
            if (annotations.length == 1) {
                return Map.of("autowired", "");
            }
            for (Annotation temp : annotations) {
                if (temp instanceof Qualifier qualifier) {
                    return Map.of("qualifier", qualifier.value());
                }
            }
        }
        return Map.of("set", field.getName());
    }

    private void initBeanInstance(List<File> list) {
        for (File file : list) {
            //basePackage
            if (file.isFile() && file.getName().endsWith(".class")) {
                try {
                    String standardPath = file.getCanonicalPath();
                    String className = standardPath.
                            substring(standardPath.lastIndexOf(basePackage.replaceAll("\\.", "\\\\"))
                                    + basePackage.length() + 1);
                    className = basePackage + "." + className.substring(0, className.length() - ".class".length()).replaceAll("\\\\", ".");
                    Class<?> aClass = Class.forName(className);
                    String beanId = isAnnotationed(aClass); //
//                    beanId ！= null --> init
                    if (beanId == null) {
                        continue;
                    }
                    Object obj = aClass.getDeclaredConstructor().newInstance();
                    webContext.put(beanId, obj);
                } catch (IOException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                         IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else if (file.isDirectory()) {
                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                    initBeanInstance(Collections.singletonList(listFile));
                }
            }
        }
    }

    private String isAnnotationed(Class<?> aClass) {
        Annotation[] annotations = aClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Controller controller) {
                String value = controller.value();
                if ("".equals(value)) {
                    return String.valueOf(aClass.getSimpleName().charAt(0)).toLowerCase() + aClass.getSimpleName().substring(1);
                }
                return value;
            } else if (annotation instanceof RestController restController) {
                String value = restController.value();
                if ("".equals(value)) {
                    return String.valueOf(aClass.getSimpleName().charAt(0)).toLowerCase() + aClass.getSimpleName().substring(1);
                }
                return value;
            }
        }
        return null;
    }

    @Override
    public Object getBean(String beanId) {
        return null;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return null;
    }
}
