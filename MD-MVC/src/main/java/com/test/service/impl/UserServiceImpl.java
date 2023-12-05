package com.test.service.impl;

import com.test.bean.Student;
import com.test.service.UserService;
import com.ls.stereotype.Autowired;
import com.ls.stereotype.Qualifier;
import com.ls.stereotype.Resource;
import com.ls.stereotype.Service;

/**
 * @author Lartimes
 * @version 1.0
 * @description:
 * @since 2023/10/21 14:49
 */
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    @Qualifier("student")
    private Student student;

   @Resource(name = "student")
    private Student student2;

    @Override
    public String toString() {
        return "UserServiceImpl{" +
                "student=" + student +
                ", student2=" + student2 +
                '}';
    }
}
