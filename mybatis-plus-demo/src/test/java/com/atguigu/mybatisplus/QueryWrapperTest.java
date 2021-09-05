package com.atguigu.mybatisplus;

import com.atguigu.entity.User;
import com.atguigu.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class QueryWrapperTest {
    @Resource
    private UserMapper userMapper;

    //查询名字中包含n，年龄大于等于10且小于等于20，email不为空的用户
    @Test
    public void fun1(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like("name", "n")
                .between("age", 10, 30)
                .isNotNull("email");
        List<User> users = userMapper.selectList(userQueryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    //按年龄降序查询用户，如果年龄相同则按id升序排列
    @Test
    public void fun2(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.orderByDesc("age")
                        .orderByAsc("id");
        List<User> users = userMapper.selectList(userQueryWrapper);
        for (User user : users) {
            System.out.println(user);
        }
    }

    //删除email为空的用户
    @Test
    public void fun3(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.isNull("email");
        int res = userMapper.delete(userQueryWrapper);
        System.out.println("删除结果："+res);
    }

    //查询名字中包含n，且（年龄小于18或email为空的用户），并将这些用户的年龄设置为18，邮箱设置为 user@atguigu.com
    @Test
    public void fun4(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like("name","n")
                        .and(q->q.lt("age",18).or().eq("email",""));
        User user = new User();
        user.setAge(18);
        user.setEmail("user@atguigu.com");
        int res = userMapper.update(user, userQueryWrapper);
        System.out.println("res = " + res);
    }

    //查询所有用户的用户名和年龄
    @Test
    public void fun5(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("name","age");
        List<Map<String, Object>> maps = userMapper.selectMaps(userQueryWrapper);
    /*    for (Map<String, Object> map : maps) {
            Set<Map.Entry<String, Object>> entries = map.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                System.out.print("key: "+entry.getKey()+",value: "+entry.getValue());
                System.out.println();
            }
        }*/
    maps.forEach(System.out::println);

    }

    //查询id不大于3的所有用户的id列表
    @Test
    public void fun6(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.inSql("id","SELECT id FROM t_user WHERE age >30");
        List<Object> objects = userMapper.selectObjs(userQueryWrapper);
        System.out.println(objects);
    }

    //动态组装查询条件
    @Test
    public void fun7(){
        //定义条件
        String name = "an";
        Integer ageBegin=20;
        Integer ageEnd=50;
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like(!StringUtils.isEmpty(name),"name",name)
                        .gt(!ObjectUtils.isEmpty(ageBegin),"age",ageBegin)
                        .lt(!ObjectUtils.isEmpty(ageEnd),"age",ageEnd);
        List<User> users = userMapper.selectList(userQueryWrapper);
        users.forEach(System.out::println);
    }

    //动态组装查询条件
    @Test
    public void fun8(){

    }

}
