package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.LendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private IntegralGradeMapper integralGradeMapper;
    @Resource
    private DictService dictService;
    @Resource
    private BorrowerMapper borrowerMapper;
    @Resource
    private BorrowerService borrowerService;
    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Integer integral = userInfo.getIntegral();
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper.le("integral_start",integral);
        integralGradeQueryWrapper.ge("integral_end",integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if(integralGrade==null){
            return new BigDecimal("0");
        }

        return integralGrade.getBorrowAmount();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //判断用户绑定状态
        Assert.isTrue(userInfo.getBindStatus().intValue()== UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);
        //判断用户是否审批通过
        Assert.isTrue(userInfo.getBorrowAuthStatus().intValue()== BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);
        //判断借款额度是否足够
        Assert.isTrue(borrowInfo.getAmount().doubleValue()<=this.getBorrowAmount(userId).doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        //设置参数存储数据
        borrowInfo.setUserId(userId);
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal("100"),2, RoundingMode.HALF_DOWN));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);

    }

    @Override
    public Integer getBorrowInfoStatus(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.eq("user_id", userId);
        BorrowInfo borrowInfo = baseMapper.selectOne(borrowInfoQueryWrapper);
        if(borrowInfo==null) return 0;
        return borrowInfo.getStatus();


    }

    @Override
    public List<BorrowInfo> selectBorrowInfoList() {
        List<BorrowInfo> list=baseMapper.selectBorrowInfoList();
        list.forEach(borrowInfo -> {
            Map<String,Object> param= new HashMap<>();
            param.put("returnMethod",dictService.getNameByParentDictCodeAndValue("returnMethod",borrowInfo.getReturnMethod()));
            param.put("moneyUse",dictService.getNameByParentDictCodeAndValue("moneyUse",borrowInfo.getMoneyUse()));
            param.put("status",BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus()));
            borrowInfo.setParam(param);
        });
        return list;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long borrowerInfoId) {
        BorrowInfo borrowInfo = baseMapper.selectById(borrowerInfoId);
        //完善borrowInfo信息
        borrowInfo.getParam().put("returnMethod",dictService.getNameByParentDictCodeAndValue("returnMethod",borrowInfo.getReturnMethod()));
        borrowInfo.getParam().put("moneyUse",dictService.getNameByParentDictCodeAndValue("moneyUse",borrowInfo.getMoneyUse()));
        borrowInfo.getParam().put("status",BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus()));
        //获取borrower信息并完善
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id",borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO= borrowerService.getBorrowerDetailVOById(borrower.getId());
        //封装结果返回
        Map<String,Object> map = new HashMap<>();
        map.put("borrower",borrowerDetailVO);
        map.put("borrowInfo",borrowInfo);
        return map;

    }

    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        //修改borrowInfo的审批状态
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);
        //如果审核通过则生成标的
        if(borrowInfoApprovalVO.getStatus().intValue()==BorrowInfoStatusEnum.CHECK_OK.getStatus()){
            //create lend
            lendService.createLend(borrowInfoApprovalVO,borrowInfo);
        }
    }
}
