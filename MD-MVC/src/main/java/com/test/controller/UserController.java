package com.test.controller;


import com.ls.stereotype.web.*;
import com.test.bean.Student;
import com.test.service.UserService;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/21 14:49
 */
@RestController
public class UserController {


    private Student student;
    private UserService userService;

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String toString() {
        return "UserController{" +
                "student=" + student +
                ", userService=" + userService +
                '}';
    }

    @GetMapping("/user/doAA")
    public String doA() {
        System.out.println("doA=============================================");
        return null;
    }
    @ResponseBody
    @RequestMapping(value = "/user/doB", method = "POST")
    public Object doB(@RequestBody Student student,
                     @RequestParam("id") Integer id) {
        return student;//{"id" : 1,"name" : "reg","clazz" : {"id" : 1,"clazzName" : "ewgwe"}}
    }

    @RequestMapping(value = "/user/doC" , method = "POST")
    public Object doC(@RequestBody Student student,
                      @RequestParam("name")String name) {
        student.setName(name);
        return student;
//        object --》 HttpMessageConverter
    }
}
