package com.atguigu.srb;


import com.atguigu.srb.core.ServiceCoreApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(classes = ServiceCoreApplication.class)
public class TestRedis {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1(){
        BoundValueOperations valueOps = redisTemplate.boundValueOps("name");
        valueOps.set("zhangsan");
        String name= (String) valueOps.get();
        System.out.println("name = " + name);
    }
}
