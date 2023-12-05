package com.ls.utils;

import java.io.InputStream;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/22 0:52
 */
public class Resources {
    private Resources(){}

    public static InputStream getResourcesAsStream(String path){
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
