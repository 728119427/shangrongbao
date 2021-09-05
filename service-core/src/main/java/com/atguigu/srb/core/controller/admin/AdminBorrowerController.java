package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("借款人管理")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/core/borrower")
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "查询关键字",required = false) @RequestParam("keyword") String keyword,
            @ApiParam(value = "查询页码",required = true) @PathVariable("page") Long page,
            @ApiParam(value = "每页记录数",required = true) @PathVariable("limit") Long limit) {

        Page<Borrower> pageParam = new Page<>(page, limit);
        IPage<Borrower> pageModel=borrowerService.listPage(pageParam,keyword);
        return R.ok().data("pageModel",pageModel);
    }


    @ApiOperation("显示借款人信息")
    @GetMapping("/show/{id}")
    public R show(@ApiParam(value = "借款人id",required = true) @PathVariable("id") Long borrowerId){
        BorrowerDetailVO borrowerDetailVO=borrowerService.getBorrowerDetailVOById(borrowerId);
        return R.ok().data("borrowerDetailVo",borrowerDetailVO);
    }

    @ApiOperation("审核借款人信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO){
        borrowerService.approval(borrowerApprovalVO);
        return R.ok().message("审核完成!");
    }


}

