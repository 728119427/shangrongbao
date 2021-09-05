package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Api("数据字典")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/core/dict")
public class DictController {

    @Resource
    private DictService dictService;

    @ApiOperation(("根据dictCode获取下级节点"))
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(
            @ApiParam(value = "节点编码",required = true)
            @PathVariable("dictCode") String dictCode){
        List<Dict> dictList = dictService.findByDictCode(dictCode);
        return R.ok().data("dictList",dictList);
    }
}

