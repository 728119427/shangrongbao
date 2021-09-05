package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.IntegralEnum;
import com.atguigu.srb.core.mapper.BorrowerAttachMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.mapper.UserIntegralMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.pojo.entity.BorrowerAttach;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserIntegral;
import com.atguigu.srb.core.pojo.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.vo.BorrowerAttachVO;
import com.atguigu.srb.core.pojo.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerAttachService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.log.BaseMetricsHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;
    @Resource
    private DictService dictService;
    @Resource
    private BorrowerAttachService borrowerAttachService;
    @Resource
    private UserIntegralMapper userIntegralMapper;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveBorrowerByUserId(BorrowerVO borrowerVO, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //保存userInfo
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO,borrower);
        borrower.setUserId(userId);
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setName(userInfo.getName());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        baseMapper.insert(borrower);

        //保存附件信息
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });
        //更新userInfo的状态信息
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);

    }

    @Override
    public Integer getBorrowerStatus(Long userId) {
        //根据userId查询borrower
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", userId);
        Borrower borrower = baseMapper.selectOne(borrowerQueryWrapper);
        if(borrower==null){
            //尚未有借款额度申请，即还没开通借款，此时还没创建borrower
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        return borrower.getStatus();

    }

    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword) {

        if(StringUtils.isEmpty(keyword)){
            return  baseMapper.selectPage(pageParam,null);
        }
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.like("name",keyword)
                       .or().like("id_card",keyword)
                       .or().like("mobile",keyword);
        return baseMapper.selectPage(pageParam,borrowerQueryWrapper);
    }


    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long borrowerId) {
        Borrower borrower = baseMapper.selectById(borrowerId);
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        if(borrower==null){
            return borrowerDetailVO;
        }
        //填充基本信息
        BeanUtils.copyProperties(borrower,borrowerDetailVO);

        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry()?"是":"否");
        //性别
        borrowerDetailVO.setSex(borrower.getSex()==1?"男":"女");
        //学历 行业 收入 还款来源 联系人
        borrowerDetailVO.setEducation(dictService.getNameByParentDictCodeAndValue("education",borrower.getEducation()));
        borrowerDetailVO.setIndustry(dictService.getNameByParentDictCodeAndValue("industry",borrower.getIndustry()));
        borrowerDetailVO.setIncome(dictService.getNameByParentDictCodeAndValue("income",borrower.getIncome()));
        borrowerDetailVO.setReturnSource(dictService.getNameByParentDictCodeAndValue("returnSource",borrower.getReturnSource()));
        borrowerDetailVO.setContactsRelation(dictService.getNameByParentDictCodeAndValue("relation",borrower.getContactsRelation()));

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);
        //附件列表
        QueryWrapper<BorrowerAttach> borrowerAttachQueryWrapper = new QueryWrapper<>();
        borrowerAttachQueryWrapper.eq("borrower_id",borrower.getId());
        List<BorrowerAttach> borrowerAttachList = borrowerAttachMapper.selectList(borrowerAttachQueryWrapper);
        List<BorrowerAttachVO> borrowerAttachVOList = new ArrayList<>();
        borrowerAttachList.forEach(borrowerAttach -> {
            BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
            borrowerAttachVO.setImageType(borrowerAttach.getImageType());
            borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());
            borrowerAttachVOList.add(borrowerAttachVO);
        });

        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);
        return borrowerDetailVO;

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        //更新借款人得认证状态
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        Borrower borrower = baseMapper.selectById(borrowerId);
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);
        //获取用户即借款人得现有积分
        Long userId = borrower.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        BigDecimal currentIntegral= new BigDecimal(userInfo.getIntegral()+"");

        UserIntegral userIntegral = new UserIntegral();
        if(borrowerApprovalVO.getStatus()==2){
            //添加借款人基本信息积分
            BigDecimal infoIntegral = new BigDecimal(borrowerApprovalVO.getInfoIntegral()+"");
            currentIntegral = currentIntegral.add(infoIntegral);

            userIntegral.setContent("借款人基本信息");
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
            userIntegralMapper.insert(userIntegral);
            //添加借款人身份证信息积分
            if(borrowerApprovalVO.getIsIdCardOk()){
                BigDecimal idCardIntegral = new BigDecimal(IntegralEnum.BORROWER_IDCARD.getIntegral()+"");
                currentIntegral = currentIntegral.add(idCardIntegral);
                userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
                userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
                userIntegralMapper.insert(userIntegral);
            }
            //添加借款人房产信息积分
            if(borrowerApprovalVO.getIsHouseOk()){
                BigDecimal houseIntegral = new BigDecimal(IntegralEnum.BORROWER_HOUSE.getIntegral()+"");
                currentIntegral = currentIntegral.add(houseIntegral);
                userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
                userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
                userIntegralMapper.insert(userIntegral);
            }
            //添加借款人车辆信息积分
            if(borrowerApprovalVO.getIsCarOk()){
                BigDecimal carIntegral = new BigDecimal(IntegralEnum.BORROWER_CAR.getIntegral()+"");
                currentIntegral = currentIntegral.add(carIntegral);
                userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
                userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
                userIntegralMapper.insert(userIntegral);
            }
        }


        //更新userinfo得认证状态和积分信息
        userInfo.setIntegral(currentIntegral.intValue());
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        userInfoMapper.updateById(userInfo);
    }
}
