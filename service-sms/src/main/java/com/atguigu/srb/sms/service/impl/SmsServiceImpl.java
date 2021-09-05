package com.atguigu.srb.sms.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.sms.service.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void send(String mobile, String templateCode, Map<String, Object> param) {
      //创建远程连接客户端对象
        DefaultProfile profile = DefaultProfile.getProfile( SmsProperties.REGION_Id,
                                                            SmsProperties.KEY_ID,
                                                            SmsProperties.KEY_SECRET);
        DefaultAcsClient acsClient = new DefaultAcsClient(profile);

        //创建远程连接的请求参数
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId",SmsProperties.REGION_Id);
        request.putQueryParameter("PhoneNumbers",mobile);
        //这里从配置文件读取中文有问题
        String signName = "北京课时教育";
        request.putQueryParameter("SignName",signName);
        request.putQueryParameter("TemplateCode", templateCode);
        //将验证码转换为json
        Gson gson = new Gson();
        String codeJson = gson.toJson(param);
        request.putQueryParameter("TemplateParam",codeJson);

        //发送请求获取响应
        try {
            CommonResponse response = acsClient.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            Assert.isTrue(success, ResponseEnum.ALIYUN_RESPONSE_FAIL);

            String data = response.getData();
            Map map = gson.fromJson(data, Map.class);
            String code= (String) map.get("Code");
            String message= (String) map.get("Message");
            log.info("阿里云响应结果：code={},message={}",code,message);

            //ALIYUN_SMS_LIMIT_CONTROL_ERROR(-502, "短信发送过于频繁"),//业务限流
            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL", code, ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
            //ALIYUN_SMS_ERROR(-503, "短信发送失败"),//其他失败
            Assert.equals("OK", code, ResponseEnum.ALIYUN_SMS_ERROR);
        } catch (ClientException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        }


    }
}
