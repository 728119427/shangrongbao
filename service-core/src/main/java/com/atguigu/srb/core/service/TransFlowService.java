package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
public interface TransFlowService extends IService<TransFlow> {

    void saveTrasFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);
}
