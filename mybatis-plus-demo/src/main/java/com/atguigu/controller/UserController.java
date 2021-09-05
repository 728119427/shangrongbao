package com.atguigu.controller;

import com.atguigu.entity.User;
import com.atguigu.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin//解决跨域
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/list")
    public List<User> getList(){
        return userService.list();
    }
}
