package com.atguigu.mybatisplus;

import com.atguigu.entity.User;
import com.atguigu.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class MyBatisPlusTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void testSelect(){
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void tsetInsert(){
        User zhangsan = new User(null, "zhangsan", "123@qq.com", 22,null,null,0);
        int insert = userMapper.insert(zhangsan);
        System.out.println("insert = " + insert);
        System.out.println("id:"+zhangsan.getId());
    }

    @Test
    public void tetsUpdate(){
        User user = new User();
        user.setName("李四");
        user.setAge(18);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name","Jone");
        userMapper.update(user,queryWrapper);
    }

    @Test
    public void testDelete(){
        userMapper.deleteById(3);

    }

    @Test
    public void testFindAll(){
        List<User> all = userMapper.findAll();
        for (User user : all) {
            System.out.println("user = " + user);
        }
    }
}
