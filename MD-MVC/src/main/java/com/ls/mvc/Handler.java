package com.ls.mvc;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Lartimes
 * @version 1.0
 * @description: URI 映射对象
 * @since 2023/10/24 12:39
 */

public class Handler {
    public Handler() {
    }
    private Class[] parameters;
    private Object obj;
    private String methodType;
    private String methodName;

    public Handler(Class[] parameters, Object obj, String methodType, String methodName) {
        this.parameters = parameters;
        this.obj = obj;
        this.methodType = methodType;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Handler handler = (Handler) o;
        return Arrays.equals(parameters, handler.parameters) && Objects.equals(obj, handler.obj) && Objects.equals(methodType, handler.methodType) && Objects.equals(methodName, handler.methodName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(obj, methodType, methodName);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }

    public Class[] getParameters() {
        return parameters;
    }

    public void setParameters(Class[] parameters) {
        this.parameters = parameters;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "Handler{" +
                "parameters=" + Arrays.toString(parameters) +
                ", obj=" + obj +
                ", methodType='" + methodType + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
