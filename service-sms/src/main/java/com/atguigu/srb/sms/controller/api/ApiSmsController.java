package com.atguigu.srb.sms.controller.api;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.RandomUtils;
import com.atguigu.srb.common.util.RegexValidateUtils;
import com.atguigu.srb.sms.client.CoreUserInfoClient;
import com.atguigu.srb.sms.service.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin //解决跨域
@Slf4j
@Api(tags = "短信管理")
public class ApiSmsController {
    @Resource
    private SmsService smsService;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("发送短信获取验证码")
    @GetMapping("/send/{mobile}")
    public R sendMessage(
            @ApiParam(value = "手机号",required = true)
            @PathVariable("mobile") String mobile){
        //先对手机号码进行验证
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);//手机是否为空
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile),ResponseEnum.MOBILE_ERROR);//手机格式是否正确
        //手机是否被注册,true未被注册，false已注册
     /*   boolean b = coreUserInfoClient.checkMobile(mobile);
        Assert.isTrue(b,ResponseEnum.MOBILE_EXIST_ERROR);*/

        //生成验证码
        String validateCode = RandomUtils.getFourBitRandom();
        Map<String,Object> param = new HashMap<>();
        param.put("code",validateCode);
        //发送短信
        smsService.send(mobile, SmsProperties.TEMPLATE_CODE,param);
        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:"+mobile,validateCode,5l, TimeUnit.MINUTES);
        return R.ok().message("短信发送成功");
    }
}
