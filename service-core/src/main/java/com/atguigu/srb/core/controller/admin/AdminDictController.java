package com.atguigu.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "数据字典管理")
@CrossOrigin
@RequestMapping("/admin/core/dict")
public class AdminDictController {
    @Resource
    private DictService dictService;

    @PostMapping("/import")
    @ApiOperation("批量导入数据字典")
    public R batchImport(@ApiParam(value = "导入的excel文件",required = true)@RequestParam(value = "file",required = true) MultipartFile file){
        try {
            InputStream inputStream = file.getInputStream();
            dictService.importData(inputStream);
            return R.ok().message("批量导入数据成功!");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
    }


    @ApiOperation("导出数据字典")
    @GetMapping("/export")
    public R exportData(HttpServletResponse response){
        try {
            //设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
            //导出
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());
            return R.ok().message("导出成功");
        } catch (Exception e) {
            return R.setResult(ResponseEnum.EXPORT_DATA_ERROR) ;
        }
    }

    @ApiOperation("根据父id查询字典")
    @GetMapping("/listByParentId/{parentId}")
    public R listByParentId(@ApiParam(value = "父id",required = true)@PathVariable("parentId") Long id){
        List<Dict> list=dictService.listByParentId(id);
        return R.ok().data("list",list);
    }
}
