package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api("会员管理")
@RestController
@RequestMapping("/admin/core/userInfo")
@CrossOrigin
@Slf4j
public class AdminUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("会员分页列表")
    @PostMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "查询页码",required = true) @PathVariable("page") Long page,
            @ApiParam(value = "每页记录数",required = true)@PathVariable("limit") Long limit,
            @ApiParam(value = "查询条件",required = false)@RequestBody UserInfoQuery userInfoQuery){

            Page<UserInfo> pageParam = new Page<>(page,limit);
            IPage<UserInfo> pageModel=userInfoService.listPage(pageParam,userInfoQuery);
            return R.ok().data("pageModel",pageModel);
    }

    @ApiOperation("锁定和解锁")
    @PutMapping("/lock/{id}/{status}")
    public R lock(
            @ApiParam(value = "会员id",required = true)@PathVariable("id") Long id,
            @ApiParam(value = "锁定状态(0锁定,1解锁)",required = true)@PathVariable("status")Integer status)
    {
            userInfoService.lock(id,status);
            return R.ok().message(status==1?"解锁成功":"锁定成功");
    }
}
