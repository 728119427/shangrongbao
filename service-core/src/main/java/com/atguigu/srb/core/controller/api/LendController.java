package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
@RequestMapping("/api/core/lend")
public class LendController {
    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("list")
    public R getList(){
        List<Lend> list = lendService.getList();
        return R.ok().data("lendList",list);
    }


    @ApiOperation("标的详情")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "标的id",required = true) @PathVariable("id") Long lendId){
        Map<String,Object> lendDetail=lendService.getLendDeatil(lendId);
        return R.ok().data("lendDetail",lendDetail);
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalMonth}/{returnMethod}")
    public  R getInterestCount(
            @ApiParam(value = "投资金额",required = true)@PathVariable("invest") BigDecimal invest,
            @ApiParam(value = "年化收益",required = true)@PathVariable("yearRate") BigDecimal yearRate,
            @ApiParam(value = "投资期数",required = true)@PathVariable("totalMonth") Integer totalMonth,
            @ApiParam(value = "还款方式",required = true)@PathVariable("returnMethod") Integer returnMethod
    ){
        BigDecimal interestCount=lendService.getIntersetCount(invest,yearRate,totalMonth,returnMethod);
        return R.ok().data("interestCount",interestCount);
    }



}

