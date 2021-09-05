package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.vo.InvestVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
public interface LendItemService extends IService<LendItem> {

    String commitInvest(InvestVO investVO);

    void investNotify(Map<String, Object> paramMap);

    List<LendItem> getLendItemListByLendId(Long lendId, Integer status);
}
