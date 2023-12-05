package com.test;

import com.ls.context.ApplicationContext;
import com.ls.stereotype.ComponentScan;
import com.test.bean.Student;
import com.ls.bootStarter.MVCBootApplication;
import com.ls.context.ClassPathApplicationContext;
import com.ls.stereotype.SpringBootApplication;
import com.test.service.impl.UserServiceImpl;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/21 11:32
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.test.bean", "com.test.service"})
public class Application {
    public static void main(String[] args) throws Exception {
        ApplicationContext start = new Application().start();
        UserServiceImpl userServiceImpl = start.getBean("userServiceImpl", UserServiceImpl.class);
        System.out.println(userServiceImpl);
    }

    public ApplicationContext start() throws Exception{
         return MVCBootApplication.run(this.getClass(), "spring.xml");
    }

}
