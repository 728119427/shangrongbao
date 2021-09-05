package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("会员账户")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/core/userAccount")
public class UserAccountController {

    @Resource
    private UserAccountService userAccountService;

    @ApiOperation("会员充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public R commitCharge(@ApiParam("充值金额") @PathVariable("chargeAmt") BigDecimal chargeAmt, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitCharge(chargeAmt,userId);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("用户充值异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = RequestHelper.switchMap(parameterMap);
        log.info("用户充值异步回调："+ JSON.toJSONString(paramMap));

        //校验签名
        if(RequestHelper.isSignEquals(paramMap)){
            if("0001".equals(paramMap.get("resultCode"))){
                //充值成功
                return userAccountService.notify(paramMap);
            }else {
                //充值失败
                log.info("用户充值异步回调充值失败："+ JSON.toJSONString(paramMap));
                return "success";
            }

        }else {
            log.info("用户充值异步回调签名错误:"+ JSON.toJSONString(paramMap));
            return "fail";
        }
    }


    @ApiOperation("查询账户余额")
    @GetMapping("/auth/getAccount")
    public R getAccount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal account=userAccountService.getAccount(userId);
        return R.ok().data("account",account);
    }

}

