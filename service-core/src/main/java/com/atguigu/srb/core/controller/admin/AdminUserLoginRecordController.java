package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.mapper.UserLoginRecordMapper;
import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.atguigu.srb.core.service.UserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api("会员登录日志接口")
@Slf4j
@CrossOrigin//跨域
@RestController
@RequestMapping("/admin/core/userLoginRecord")
public class AdminUserLoginRecordController {
    @Resource
    private UserLoginRecordService userLoginRecordService;

    @ApiOperation("查询会员登录日志")
    @GetMapping("listTop50/{userId}")
    public R listTop50(@ApiParam(value = "会员id",required = true)@PathVariable("userId") Long userId){
        List<UserLoginRecord> loginRecordList=userLoginRecordService.listTop50(userId);
        return R.ok().data("list",loginRecordList);
    }
}
