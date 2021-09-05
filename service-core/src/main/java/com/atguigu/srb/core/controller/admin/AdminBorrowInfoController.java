package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("借款信息管理")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/core/borrowInfo")
public class AdminBorrowInfoController {
    @Resource
    private BorrowInfoService borrowInfoService;



    @ApiOperation("获取借款申请列表")
    @GetMapping("/list")
    public R list(){
        List<BorrowInfo> list=borrowInfoService.selectBorrowInfoList();
        return R.ok().data("list",list);
    }

    @ApiOperation("获取借款详细信息")
    @GetMapping("/show/{id}")
    public  R show(@ApiParam("借款信息id") @PathVariable("id") Long borrowerInfoId){
        Map<String,Object> borrowInfoDetail=borrowInfoService.getBorrowInfoDetail(borrowerInfoId);
        return R.ok().data("borrowInfoDetail",borrowInfoDetail);
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO){
        borrowInfoService.approval(borrowInfoApprovalVO);
        return R.ok().message("审批完成!");
    }
}

