package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Resource
    private DictMapper dictMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class,new ExcelDictDTOListener(dictMapper)).sheet().doRead();
    }

    @Override
    public List listDictData() {
        List<Dict> dictList = dictMapper.selectList(null);
        List<ExcelDictDTO> list = new ArrayList<>();
        for (Dict dict : dictList) {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict,excelDictDTO);
            list.add(excelDictDTO);
        }
        return list;
    }

    @Override
    public List<Dict> listByParentId(Long id) {

        List<Dict> dicts = null;
        //先从redis中取值
        try {
            dicts= (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictList:"+id);
            if(dicts!=null){
                log.info("从reids中取值");
                return dicts;
            }
        } catch (Exception e) {
            log.error("redis服务器异常:"+e.getMessage());
        }

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id",id);
        dicts = dictMapper.selectList(dictQueryWrapper);
        dicts.forEach(d->{
            boolean b= hasChildren(d.getId());
            d.setHasChildren(b);
        });
        //查询后放入缓存中
        try {
            redisTemplate.opsForValue().set("srb:core:dictList:"+id,dicts,5, TimeUnit.MINUTES);
            log.info("数据存入redis");
        } catch (Exception e) {
            log.error("redis服务器异常:"+e.getMessage());
        }
        return dicts;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        Long dictId = dict.getId();

        dictQueryWrapper.clear();
        dictQueryWrapper.eq("parent_id",dictId);
        List<Dict> dicts = baseMapper.selectList(dictQueryWrapper);
        return dicts;

    }

    @Override
    public String getNameByParentDictCodeAndValue(String education, Integer valueId) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code",education);
        Dict parentDict = baseMapper.selectOne(dictQueryWrapper);
        if(parentDict==null) return "";

        Long parentDictId = parentDict.getId();
        dictQueryWrapper.clear();
        dictQueryWrapper.eq("parent_id",parentDictId)
                        .eq("value",valueId);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        return dict==null?"":dict.getName();

    }

    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id",id);
        Integer integer = dictMapper.selectCount(dictQueryWrapper);
        return integer > 0;
    }

}
