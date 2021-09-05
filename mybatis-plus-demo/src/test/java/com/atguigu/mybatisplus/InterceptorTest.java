package com.atguigu.mybatisplus;

import com.atguigu.entity.User;
import com.atguigu.mapper.UserMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class InterceptorTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void testPagination(){
        IPage<User> iPage = new Page<>(2,3);
        IPage<User> userIPage = userMapper.selectPage(iPage, null);
        List<User> userList = userIPage.getRecords();
        for (User user : userList) {
            System.out.println(user);
        }

    }

    @Test
    public void testCustomizePagination(){
        IPage<User> iPage = new Page<>(2,3);
        IPage<User> selectByAge = userMapper.selectByAge(iPage, 30);
        List<User> userList = selectByAge.getRecords();
        for (User user : userList) {
            System.out.println(user);
        }
    }

}
