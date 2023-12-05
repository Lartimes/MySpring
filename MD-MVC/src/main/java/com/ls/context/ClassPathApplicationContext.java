package com.ls.context;

import com.ls.cons.IoCConstants;
import com.ls.stereotype.*;
import com.ls.utils.ClassUtiils;
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
import java.net.URL;
import java.util.*;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/20 22:45
 */
public class ClassPathApplicationContext implements ApplicationContext {
    public Map<String, Object> rawContext = new HashMap<>();
    private String basePackage;

    public ClassPathApplicationContext() {
    }

    public ClassPathApplicationContext(String basePackage, Map<String, Object> rawContext) {
        this.basePackage = basePackage;
        this.rawContext = rawContext;
    }

    /**
     * 解析xml
     * @param paths
     */
    public ClassPathApplicationContext(String... paths) {
        SAXReader saxReader = new SAXReader();
        for (String path : paths) {
            try {
                Document document = saxReader.read(Resources.getResourcesAsStream(path));
                solveDoc(document);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Map<String, Object> getRawContext() {
        return rawContext;
    }

    public void setRawContext(Map<String, Object> rawContext) {
        this.rawContext = rawContext;
    }


    /**
     * dom4j  + jaxen 依赖
     * bean --> singleton 单例模式
     *  先实例化 --> 后面在进行DependencyInjection
     *  AutoWired  Resource Qualifier Set注入 --》 四种方式
     * @param document
     */
    private void solveDoc(Document document) {
        List list = document.selectNodes("/beans/bean");
        list.forEach((temp) -> {
            Element element = (Element) temp;
            String beanId = element.attributeValue("id");
            String clazzName = element.attributeValue("class");
            try {
                Class<?> aClass = Class.forName(clazzName);
                Object obj = aClass.getDeclaredConstructor().newInstance();
                rawContext.put(beanId, obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
//        setInjection 基于set注入
        rawContext.forEach((k, v) -> {
            Element selectSingleNode = (Element) document.selectSingleNode("/beans/bean[@id='" + k + "']");
            List properties = selectSingleNode.elements("property");
            var ref = new Object() {
                final Class<?> aClass = v.getClass();
            };
            properties.forEach((property) -> {
                Element element = (Element) property;
                String name = element.attributeValue("name");
                String value = element.attributeValue("value");
//                System.out.println(name + " : " + value);
                String methodName = "set" + String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
                try {
                    Field field = ref.aClass.getDeclaredField(name);
                    Class<?> type = field.getType();
                    Method method = ref.aClass.getDeclaredMethod(methodName, type);
                    if (ClassUtiils.isPrimitiveTypeOrWrapper(type)) {//----基本类型
                        if (type.isPrimitive()) {
                            type = ClassUtiils.getPrimitiveType(type);
                        }
                        Object invoke = value;
                        if (!type.equals(String.class)) {
                            Method valueOf = type.getMethod("valueOf", String.class);
                            invoke = valueOf.invoke(null, value);
                        }
                        method.invoke(v, invoke);
                    } else if (value == null) {
                        value = element.attributeValue("ref");
//                        System.out.println(methodName); //--- 引用类型
                        method.invoke(v, rawContext.get(value));
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                         NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }


    @Override
    public String getApplicationName() {
        return IoCConstants.CLASS_CONTEXT_NAME;
    }


    /**
     * rawContext容器 与  WebContext容器分隔开，
     * MVC架构，上不 依赖下 ， 所以倒叙加载， 最后交由DispatcherServlet处理。。
     * 先写出IoC容器，先不写MVC
     */
    @Override
    public void run(Class clazz, String[] basePackages) throws IOException {
        String packageName = clazz.getPackageName();
        basePackage = packageName;
        List<File> files = new ArrayList<>();
        if (basePackages.length == 1 && "".equals(basePackages[0])) {
            System.out.println(basePackage);
            String nowClassName = clazz.getName();
            nowClassName = nowClassName.substring(nowClassName.lastIndexOf(".") + 1);
            packageName = packageName.replaceAll("\\.", "/");
            System.out.println(packageName);
            String path = Objects.requireNonNull(clazz.getClassLoader().getResource(packageName)).getPath();
            File file = new File(path);
            String finalNowClassName = nowClassName;

            files = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter((fl) -> {
                boolean flag = true;
                if (fl.isFile()) {
                    String name = fl.getName();
                    if (name.length() > finalNowClassName.length()) {
                        if (".class".equals(name.substring(finalNowClassName.length()))) {
                            flag = false;
                        }
                    }
                }
                return flag;
            }).toList();
            initBeanInstance(files);
        } else {
            for (String basePackage : basePackages) {
                System.out.println(basePackage);
                URL resource = Thread.currentThread().getContextClassLoader()
                        .getResource(basePackage.replaceAll("\\.", "/"));
                files.add(new File(resource.getPath()));
            }
            initBeanInstance(files);
        }


//        setInjection-->
//     autowired qualifier ---> byType
//        resource  ---> byName
//        配置文件与注解分隔，配置文件以及init 完毕
        dependencyInjection(); //----注解init


        /*原生容器已经加载完毕*/
//        rawContext.forEach((k, v) -> System.out.println(k + "==" + v));
    }

    @Override
    public Object getBean(String beanId) {
        Object o = rawContext.get(beanId);
        Objects.requireNonNull(o);
        return o;
    }

    @Override
    public <T> T getBean(String beanId, Class<T> requiredType) {
        T o = (T) rawContext.get(beanId);
        Objects.requireNonNull(o);
        return o;
    }


    private void dependencyInjection() { //----只对注解类进行DI
        /*
        1. Autowired type
        2.Resource name
        3.set注入 type
        优先级如上
        * */
        rawContext.forEach((k, v) -> {
            Class<?> aClass = v.getClass();
            if (!Objects.isNull(isAnnotationed(aClass))) {
                Field[] fields = aClass.getDeclaredFields();
                for (Field field : fields) {
                    Map<String, String> diPattern = getDIPattern(field);
                    String pattern = (String) diPattern.keySet().toArray()[0];
                    String beanId = diPattern.get(pattern);

//                    String ---> 适配器模式
//                    resource qualifier  autowired set
                    if ("resource".equals(pattern) || "qualifier".equals(pattern)) { //--- byName
                        rawContext.forEach((id, clazz) -> {
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
                        List<Object> list = rawContext.values().stream().filter((clazz) -> clazz.getClass().equals(field.getType())).toList();
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
                        List<Object> list = rawContext.values().stream().filter((clazz) -> clazz.getClass().equals(field.getType())).toList();
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
                return Map.of("autowired", null);
            }
            for (Annotation temp : annotations) {
                if (temp instanceof Qualifier qualifier) {
                    return Map.of("qualifier", qualifier.value());
                }
            }
        }
        return Map.of("set", field.getName());
    }


    /**
     * 文件就实例化
     * 目录就递归
     * @param list
     */
    private void initBeanInstance(List<File> list) throws IOException {
        for (File file : list) {
            String standardPath = file.getCanonicalPath();
            if (file.isFile() && standardPath.endsWith(".class")) {//文件
                String className = standardPath.
                        substring(standardPath.lastIndexOf(basePackage.replaceAll("\\.", "\\\\"))
                                + basePackage.length() + 1);
                className = basePackage + "." + className.substring(0, className.length() - ".class".length()).replaceAll("\\\\", ".");
                try {
                    System.out.println(className);
                    Class<?> aClass = Class.forName(className);
                    String beanId = isAnnotationed(aClass);
                    if (Objects.isNull(beanId)) {
                        continue;
                    }
                    Object obj = aClass.getDeclaredConstructor().newInstance();
                    rawContext.put(beanId, obj);//-----提前曝光
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (file.isDirectory()) { //目录
                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                    initBeanInstance(Collections.singletonList(listFile));
                }
            }
        }
    }

    private String isAnnotationed(Class<?> aClass) {
        Annotation[] annotations = aClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Component component) {
                String value = component.value();
                if ("".equals(value)) {
                    return String.valueOf(aClass.getSimpleName().charAt(0)).toLowerCase() + aClass.getSimpleName().substring(1);
                }
                return value;
            } else if (annotation instanceof Service service) {
                String value = service.value();
                if ("".equals(value)) {
                    return String.valueOf(aClass.getSimpleName().charAt(0)).toLowerCase() + aClass.getSimpleName().substring(1);
                }
                return value;
            } else if (annotation instanceof Repository repository) {
                String value = repository.value();
                if ("".equals(value)) {
                    return String.valueOf(aClass.getSimpleName().charAt(0)).toLowerCase() + aClass.getSimpleName().substring(1);
                }
                return value;
            }
        }
        return null;
    }


}
