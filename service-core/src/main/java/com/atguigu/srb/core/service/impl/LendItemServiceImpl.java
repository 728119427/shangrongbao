package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.mapper.LendItemMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.vo.InvestVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.LendNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Resource
    private LendMapper lendMapper;
    @Resource
    private LendService lendService;
    @Resource
    private UserAccountService userAccountService;
    @Resource
    private UserBindService userBindService;
    @Resource
    private TransFlowService transFlowService;
    @Resource
    private UserAccountMapper userAccountMapper;



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String commitInvest(InvestVO investVO) {
        //获取标的信息
        Long lendId = investVO.getLendId();
        Lend lend = lendMapper.selectById(lendId);
        //lend状态必须为募集中
        Assert.isTrue(lend.getStatus().intValue()== LendStatusEnum.INVEST_RUN.getStatus().intValue(), ResponseEnum.LEND_INVEST_ERROR);
        //lend不能超卖
        BigDecimal nowInvest = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(lend.getAmount().doubleValue()>=nowInvest.doubleValue(),ResponseEnum.LEND_FULL_SCALE_ERROR);
        //投资金额不能超过账户金额
        BigDecimal amount = userAccountService.getAccount(investVO.getInvestUserId());
        Assert.isTrue(amount.doubleValue()>=Double.parseDouble(investVO.getInvestAmount()),ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        //生成lenditme
        LendItem lendItem = new LendItem();
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount()));
        lendItem.setInvestName(investVO.getInvestName());
        lendItem.setInvestUserId(investVO.getInvestUserId());
        lendItem.setLendItemNo(LendNoUtils.getLendItemNo());
        lendItem.setInvestTime(LocalDateTime.now());
        lendItem.setStatus(0);
        lendItem.setLendYearRate(lend.getLendYearRate());
        lendItem.setLendStartDate(lend.getLendStartDate());
        lendItem.setLendEndDate(lend.getLendEndDate());
        lendItem.setLendId(lendId);
        //预期收益
        BigDecimal expectAmout = lendService.getIntersetCount(new BigDecimal(investVO.getInvestAmount()), lend.getLendYearRate(), lend.getPeriod(), lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmout);
        lendItem.setRealAmount(new BigDecimal("0"));
        baseMapper.insert(lendItem);

        //构建hfb表单
        String investBindCode = userBindService.getBindCodeByUserId(investVO.getInvestUserId());
        String borrowerBindCode= userBindService.getBindCodeByUserId(lend.getUserId());

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", investBindCode);
        paramMap.put("benefitBindCode",borrowerBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItem.getLendItemNo());//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //生成表单
        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
        return formStr;
    }

    @Override
    public void investNotify(Map<String, Object> paramMap) {
        //判断是否已经产生流水
        String transNo= (String) paramMap.get("agentBillNo");
        boolean saveTransFlow = transFlowService.isSaveTransFlow(transNo);
        if(saveTransFlow){
            return;
        }
        //更改userAccount
        String bindCode= (String) paramMap.get("voteBindCode");
        String voteAmt= (String) paramMap.get("voteAmt");
        userAccountMapper.updateAccount(bindCode,new BigDecimal("-"+voteAmt),new BigDecimal(voteAmt));
        //修改lendItem得状态
        LendItem lendItem = getLendItemByLendItemNo(transNo);
        lendItem.setStatus(1);
        baseMapper.updateById(lendItem);
        //修改标的lend得投资人数和金额
        Lend lend = lendMapper.selectById(lendItem.getLendId());
        lend.setInvestAmount(lend.getInvestAmount().add(new BigDecimal(voteAmt)));
        lend.setInvestNum(lend.getInvestNum()+1);
        lendMapper.updateById(lend);
        //生成流水
        TransFlowBO transFlowBO = new TransFlowBO(  transNo,
                                                    bindCode,
                                                    lendItem.getInvestAmount(),
                                                    TransTypeEnum.INVEST_LOCK,
                                                    "投资项目编号："+lend.getLendNo()+"项目名称："+lend.getTitle());
        transFlowService.saveTrasFlow(transFlowBO);
    }

    @Override
    public List<LendItem> getLendItemListByLendId(Long lendId, Integer status) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_id",lendId).eq("status",status);
        return baseMapper.selectList(lendItemQueryWrapper);

    }

    private LendItem getLendItemByLendItemNo(String transNo) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_item_no",transNo);
        return baseMapper.selectOne(lendItemQueryWrapper);
    }
}
