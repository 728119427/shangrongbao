package com.atguigu.mybatisplus;

import com.atguigu.entity.User;
import com.atguigu.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestServiceImpl {
    @Resource
    private UserService userService;

    @Test
    public void fun1(){
        int count = userService.count();
        System.out.println(count);
    }

    @Test
    public void fun2(){
        List<User> users = new ArrayList<>();
        User user =null;
        for (int i = 0; i < 10; i++) {
            user= new User();
            user.setName("name"+i);
            user.setAge(i*7);
            user.setEmail("werf"+i+"@qq.com");
            users.add(user);
        }
        userService.saveBatch(users);
    }

    @Test
    public void testGetAll(){
        List<User> users = userService.getAll();
        for (User user : users) {
            System.out.println("user = " + user);
        }
    }
}
