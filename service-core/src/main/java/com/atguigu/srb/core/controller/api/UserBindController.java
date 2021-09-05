package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.pojo.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("会员账号绑定接口")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/core/userBind")
public class UserBindController {

    @Resource
    private UserBindService userBindService;

    @ApiOperation("/账号绑定提交数据")
    @PostMapping("/auth/bind")
    public R userBind(@RequestBody UserBindVO userBindVO, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr=userBindService.commitBindUser(userBindVO,userId);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("账号绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String,Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        //校验签名

        //修改绑定状态
        userBindService.notifyBind(paramMap);
        return "success";
    }

}

