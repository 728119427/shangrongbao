package com.atguigu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.atguigu.mapper")
public class MybatisPlusMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusMainApplication.class,args);
    }
}
