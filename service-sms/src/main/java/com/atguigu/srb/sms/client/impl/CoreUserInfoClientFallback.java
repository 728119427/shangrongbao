package com.atguigu.srb.sms.client.impl;

import com.atguigu.srb.sms.client.CoreUserInfoClient;
import org.springframework.stereotype.Component;


@Component
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    @Override
    public boolean checkMobile(String mobile) {
        System.err.println("请求错误，服务降级");
        return false;
    }
}
