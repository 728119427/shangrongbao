package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Api("积分等级管理")
@RestController
@CrossOrigin
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation(("获取积分等级列表"))
    @GetMapping("/list")
    public R listAll(){
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("list",list);
    }

    @ApiOperation("根据id查询积分等级")
    @GetMapping("/get/{id}")
    public R getById(@ApiParam("查询id")@PathVariable("id")Long id){
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if(!ObjectUtils.isEmpty(integralGrade)){
            return R.ok().data("record",integralGrade);
        }else {
            return R.error().message("数据不存在");
        }
    }

    @ApiOperation(value = "根据id删除积分等级",notes = "逻辑删除")
    @DeleteMapping("/remove/{id}")
    public R removebyId(@ApiParam("数据id") @PathVariable("id")Long id){
        boolean flag = integralGradeService.removeById(id);
        if(flag){
            return R.ok().message("删除成功！");
        }else {
            return R.error().message("删除失败！");
        }
    }

    @ApiOperation("新增积分等级")
    @PostMapping("/save")
    public R save(@ApiParam("积分等级对象")@RequestBody IntegralGrade integralGrade){
        BigDecimal borrowAmount = integralGrade.getBorrowAmount();
        Assert.notNull(borrowAmount,ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        boolean b = integralGradeService.save(integralGrade);
        if(b){
            return R.ok().message("新增成功");
        }else {
            return R.error().message("新增失败");
        }
    }

    @ApiOperation("根据id修改积分等级")
    @PutMapping("/update")
    public R update(@ApiParam("积分等级对象")@RequestBody IntegralGrade integralGrade){
        boolean b = integralGradeService.updateById(integralGrade);
        if(b){
            return R.ok().message("修改成功");
        }else {
            return R.error().message("修改失败");
        }
    }

}
