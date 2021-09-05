package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.pojo.vo.InvestVO;
import com.atguigu.srb.core.service.LendItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("标的投资")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/core/lendItem")
public class LendItemController {
    @Resource
    private LendItemService lendItemService;

    @ApiOperation("会员投资提交数据")
    @PostMapping("/auth/commitInvest")
    public R commitInvest(@RequestBody InvestVO investVO, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String userName = JwtUtils.getUserName(token);
        investVO.setInvestUserId(userId);
        investVO.setInvestName(userName);
        String formStr = lendItemService.commitInvest(investVO);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("会员投资异步回调")
    @PostMapping("/notify")
    public String investNOtify(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        //校验签名
        if(RequestHelper.isSignEquals(paramMap)){
            if("0001".equals(paramMap.get("resultCode"))){
                //交易成功
                lendItemService.investNotify(paramMap);
                return "success";
            }else{
                log.info("用户投资异步回调失败:"+ JSON.toJSONString(paramMap));
                return "fail";
            }
        }else {
            //签名出错
            log.info("用户回调签名错误:"+ JSON.toJSONString(paramMap));
            return "fail";
        }
    }

}

