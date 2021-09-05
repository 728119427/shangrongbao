package com.atguigu.mybatisplus;

import com.atguigu.entity.Product;
import com.atguigu.mapper.ProductMapper;
import com.atguigu.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class TestOptimisticLocker {
    @Resource
    private ProductMapper productMapper;

    @Test
    public void testOpLocker(){
        //小王查找产品价格信息
        Product product1 = productMapper.selectById(1l);
        //小李查找产品价格信息
        Product product2 = productMapper.selectById(1l);
        //小王增加50
        product1.setPrice(product1.getPrice()+50);
        productMapper.update(product1,null);
        //小李减少30
        product2.setPrice(product2.getPrice()-30);
        int update = productMapper.update(product2, null);
        if(update<1){
            Product product = productMapper.selectById(1l);
            product.setPrice(product.getPrice()-30);
            productMapper.update(product,null);
        }

        //最终结果
        Product product3 = productMapper.selectById(1l);
        System.out.println("最终的产品价格："+product3.getPrice());
    }
}
