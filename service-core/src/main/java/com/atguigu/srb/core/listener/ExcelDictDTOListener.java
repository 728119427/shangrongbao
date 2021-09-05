package com.atguigu.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    private static final int BATCH_COUNT=5;
    private DictMapper dictMapper;
    //用于存储数据
    private List<ExcelDictDTO> list = new ArrayList<>();

    //提供有参构造，方便注入dictMapper
    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    /**
     * 逐行解析每一条excel记录，调用n次
     * @param excelDictDTO
     * @param analysisContext
     */
    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {
        list.add(excelDictDTO);
        if(list.size()>=BATCH_COUNT){
            saveData(this.list);
            //清空数据
            this.list.clear();
        }
    }

    /**
     *  所有数据解析完成后调用
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //将可能遗留的数据插入到数据库
        saveData(list);
        log.info("解析完成");
    }

    /**
     * 往数据库中插入数据
     * @param list
     */
    private void saveData(List<ExcelDictDTO> list) {
        log.info("插入了{}条数据",list.size());
        dictMapper.insertBatch(list);
        log.info("插入完成");
    }
}
