package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.*;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Slf4j
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;
    @Resource
    private BorrowerMapper borrowerMapper;
    @Resource
    private BorrowerService borrowerService;
    @Resource
    private UserAccountMapper userAccountMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private LendItemService lendItemService;
    @Resource
    private TransFlowService transFlowService;
    @Resource
    private LendReturnService lendReturnService;
    @Resource
    private LendItemReturnService lendItemReturnService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setAmount(borrowInfo.getAmount());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setUserId(borrowInfo.getUserId());
        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setCheckAdminId(1l);
        lend.setCheckTime(LocalDateTime.now());
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal("100"),2, RoundingMode.HALF_DOWN));
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal("100"),2,RoundingMode.HALF_DOWN));
        lend.setPublishDate(LocalDateTime.now());
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);

        //起息日期
        LocalDate startDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        lend.setLendStartDate(startDate);
        //结束日期
        LocalDate lendEndDate = startDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        //平台收益
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal("12"), 8, RoundingMode.HALF_DOWN);
        BigDecimal expectBenefit = borrowInfo.getAmount().multiply(monthRate).multiply(new BigDecimal(borrowInfo.getPeriod() + ""));
        lend.setExpectAmount(expectBenefit);

        //修改标的状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());

        //添加到表中
        baseMapper.insert(lend);


    }

    @Override
    public List<Lend> getList() {
        List<Lend> lends = baseMapper.selectList(null);
        lends.forEach(lend -> {
            lend.getParam().put("returnMethod",dictService.getNameByParentDictCodeAndValue("returnMethod",lend.getReturnMethod()));
            lend.getParam().put("status",LendStatusEnum.getMsgByStatus(lend.getStatus()));
        });
        return lends;
    }

    @Override
    public Map<String, Object> getLendDeatil(Long lendId) {
        //获取标的详情
        Lend lend = baseMapper.selectById(lendId);
        lend.getParam().put("returnMethod",dictService.getNameByParentDictCodeAndValue("returnMethod",lend.getReturnMethod()));
        lend.getParam().put("status",LendStatusEnum.getMsgByStatus(lend.getStatus()));
        //获取借款人详情
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id",lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVOById = borrowerService.getBorrowerDetailVOById(borrower.getId());
        borrowerDetailVOById.setStatus(BorrowerStatusEnum.getMsgByStatus(borrower.getStatus()));

        Map<String,Object> param = new HashMap<>();
        param.put("lend",lend);
        param.put("borrower",borrowerDetailVOById);
        return param;
    }

    @Override
    public BigDecimal getIntersetCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {
        BigDecimal interestCount=null;
        if(returnMethod.intValue()==ReturnMethodEnum.ONE.getMethod().intValue()){
            interestCount= Amount1Helper.getInterestCount(invest,yearRate,totalMonth);
        }else if(returnMethod.intValue()==ReturnMethodEnum.TWO.getMethod().intValue()){
            interestCount= Amount2Helper.getInterestCount(invest,yearRate,totalMonth);
        }else if(returnMethod.intValue()==ReturnMethodEnum.THREE.getMethod().intValue()){
            interestCount= Amount3Helper.getInterestCount(invest,yearRate,totalMonth);
        }else if(returnMethod.intValue()==ReturnMethodEnum.FOUR.getMethod().intValue()){
            interestCount= Amount4Helper.getInterestCount(invest,yearRate,totalMonth);
        }

        return interestCount;
    }

    @Transactional
    @Override
    public void makeLoan(Long lendId) {
        //获取标的信息
        Lend lend = baseMapper.selectById(lendId);
        //构建参数调用放款接口
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode",lend.getLendNo());
        String agentBillNo = LendNoUtils.getLoanNo();
        paramMap.put("agentBillNo",agentBillNo);//放款得编号
        //计算平台收益
        BigDecimal monthRate = lend.getLendYearRate().divide(new BigDecimal("12"),8,RoundingMode.HALF_DOWN);
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()+""));
        paramMap.put("mchFee",realAmount);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign",RequestHelper.getSign(paramMap));
        log.info("放款参数："+ JSON.toJSONString(paramMap));
        //调用接口
        JSONObject makeLoanReuslt = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款返回结果："+makeLoanReuslt.toJSONString());
        //放款失败
        if(!"0000".equals(makeLoanReuslt.getString("resultCode"))){
            throw new BusinessException(makeLoanReuslt.getString("resultMsg"));
        }
        //更新标的信息
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        lend.setRealAmount(realAmount);
        baseMapper.updateById(lend);
        //获取借款人信息,更新借款人账户
        UserInfo userInfo = userInfoMapper.selectById(lend.getUserId());
        String borrowerBindCode = userInfo.getBindCode();
        String voteAmt = makeLoanReuslt.getString("voteAmt");
        userAccountMapper.updateAccount(borrowerBindCode,new BigDecimal(voteAmt),new BigDecimal("0"));
        //生成借款人放款得交易流水
        TransFlowBO transFlowBO = new TransFlowBO(agentBillNo,
                borrowerBindCode,
                new BigDecimal(voteAmt),
                TransTypeEnum.BORROW_BACK,
                "借款放款到账,标的编号：" + lend.getLendNo());
        transFlowService.saveTrasFlow(transFlowBO);
        //修改与该标的相关得投资人账户资金情况
        List<LendItem> lendItems=lendItemService.getLendItemListByLendId(lendId, LendStatusEnum.INVEST_RUN.getStatus());
        lendItems.forEach(lendItem -> {
            Long investUserId = lendItem.getInvestUserId();
            String investBindCode = userInfoMapper.selectById(investUserId).getBindCode();
            userAccountMapper.updateAccount(investBindCode,new BigDecimal("0"),lendItem.getInvestAmount().negate());
            //生成投资人相关放款流水
            TransFlowBO investTransFlowBO = new TransFlowBO(LendNoUtils.getLoanNo(),
                    investBindCode,
                    lendItem.getInvestAmount(),
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出,放款项目编号：" + lend.getLendNo()
            );
            transFlowService.saveTrasFlow(investTransFlowBO);
        });

        //生成还款和汇款计划
        repaymentPlan(lend);
    }

    /**
     * 生成回款和还款计划
     * @param lend
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void repaymentPlan(Lend lend){
        //生成还款计划表
        int len = lend.getPeriod().intValue();
        List<LendReturn> lendReturnList = new ArrayList<>();
        for(int i = 1;i<=len;i++){
            LendReturn lendReturn = new LendReturn();
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setCurrentPeriod(i);
            lendReturn.setFee(new BigDecimal("0"));
            lendReturn.setLendId(lend.getId());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));//第二个月开始还款
            lendReturn.setOverdue(false);
            lendReturn.setStatus(0);
            if(i==len){
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            //加入到lendReturnList中
            lendReturnList.add(lendReturn);
        }
        //批量插入
        lendReturnService.saveBatch(lendReturnList);
        //获取当前期数和lendReturnId对应得map，用于后续创建回款计划
        Map<Integer, Long> lendReturn_currentPeriod_id_map = lendReturnList.stream()
                .collect(Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId));

        //创建回款计划
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        List<LendItem> lendItemList = lendItemService.getLendItemListByLendId(lend.getId(), 1);
        for (LendItem lendItem : lendItemList) {
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturn_currentPeriod_id_map, lend);
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        //更新还款计划中得本金,利息和总和
        for (LendReturn lendReturn : lendReturnList) {
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumInterest= lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(lendItemReturn -> lendItemReturn.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //更新
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }
        lendReturnService.updateBatchById(lendReturnList);

    }

    /**
     * 生成回款计划
     * @param lendItemId
     * @param lendReturn_currentPeriod_id_map
     * @param lend
     * @return
     */
    private List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturn_currentPeriod_id_map, Lend lend) {
        LendItem lendItem = lendItemService.getById(lendItemId);
        //获取投资金额，投资利率，投资期数
        BigDecimal investAmount = lendItem.getInvestAmount();
        BigDecimal lendYearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        //根据不同得还款方式计算各期得利息和本金

        Map<Integer,BigDecimal> mapInterest = new HashMap<>();
        Map<Integer,BigDecimal> mapPrincipal = new HashMap<>();

        if(lend.getReturnMethod().intValue()==ReturnMethodEnum.ONE.getMethod().intValue()){
            mapInterest = Amount1Helper.getPerMonthInterest(investAmount,lendYearRate,totalMonth);
            mapPrincipal= Amount1Helper.getPerMonthPrincipal(investAmount,lendYearRate,totalMonth);
        }else if(lend.getReturnMethod().intValue()==ReturnMethodEnum.TWO.getMethod().intValue()){
            mapInterest = Amount2Helper.getPerMonthInterest(investAmount,lendYearRate,totalMonth);
            mapPrincipal= Amount2Helper.getPerMonthPrincipal(investAmount,lendYearRate,totalMonth);
        }else if(lend.getReturnMethod().intValue()==ReturnMethodEnum.THREE.getMethod().intValue()){
            mapInterest = Amount3Helper.getPerMonthInterest(investAmount,lendYearRate,totalMonth);
            mapPrincipal= Amount3Helper.getPerMonthPrincipal(investAmount,lendYearRate,totalMonth);
        }else if(lend.getReturnMethod().intValue()==ReturnMethodEnum.FOUR.getMethod().intValue()){
            mapInterest = Amount4Helper.getPerMonthInterest(investAmount,lendYearRate,totalMonth);
            mapPrincipal= Amount4Helper.getPerMonthPrincipal(investAmount,lendYearRate,totalMonth);
        }

        //开始正式创建还款计划
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> interestEntry : mapInterest.entrySet()) {
            Integer currentPeriod = interestEntry.getKey();
            Long lendReturnId = lendReturn_currentPeriod_id_map.get(currentPeriod);
            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lend.getId());
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);
            //设置利息，本金，总和
            //最后一期本金计算
            if(lendItemReturnList.size()>0 && currentPeriod.intValue()==lend.getPeriod().intValue()){
                BigDecimal sumPricipal = lendItemReturnList.stream().map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lastPricipal = lend.getInvestAmount().subtract(sumPricipal);
                lendItemReturn.setPrincipal(lastPricipal);
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            //设置本息总和
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturnList.add(lendItemReturn);
        }
        //插入到数据库中，添加lendItemReturn
        lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }
}
