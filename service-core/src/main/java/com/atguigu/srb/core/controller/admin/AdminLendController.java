package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("标的管理")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/core/lend")
public class AdminLendController {
    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("list")
    public R getList(){
        List<Lend> list = lendService.getList();
        return R.ok().data("list",list);
    }

    @ApiOperation("标的详情")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "标的id",required = true) @PathVariable("id") Long lendId){
        Map<String,Object> lendDetail=lendService.getLendDeatil(lendId);
        return R.ok().data("lendDetail",lendDetail);
    }

    @ApiOperation("放款")
    @GetMapping("/makeLoan/{id}")
    public R makeLoan(@ApiParam(value = "标的id",required = true)@PathVariable("id") Long lendId){
        lendService.makeLoan(lendId);
        return R.ok().message("放款成功!");
    }

}

