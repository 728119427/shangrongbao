package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.base.utils.JwtUtils;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.MD5;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserLoginRecordMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.atguigu.srb.core.pojo.query.UserInfoQuery;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-07-01
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private UserAccountMapper userAccountMapper;
    @Resource
    private UserLoginRecordMapper userLoginRecordMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(RegisterVO registerVO) {
        //先判断验证码是否正确
        String voCode = registerVO.getCode();
        String realCode= (String) redisTemplate.opsForValue().get("srb:sms:code:"+registerVO.getMobile());
        Assert.equals(voCode,realCode, ResponseEnum.CODE_ERROR);
        //校验手机是否已注册
 /*       QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("mobile",registerVO.getMobile());
        Integer count = baseMapper.selectCount(userInfoQueryWrapper);
        Assert.isTrue(count==0, ResponseEnum.MOBILE_EXIST_ERROR);*/
        //插入数据库
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setName(registerVO.getMobile());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setStatus(UserInfo.STATUS_NORMAL);
        userInfo.setHeadImg("https://srb-file.oss-cn-beijing.aliyuncs.com/avatar/07.jpg");
        baseMapper.insert(userInfo);

        //创建user账户
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);
    }

    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Integer userType = loginVO.getUserType();

        //判断该用户是否存在
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("mobile",mobile);
        userInfoQueryWrapper.eq("user_type",userType);
        UserInfo userInfoDb = baseMapper.selectOne(userInfoQueryWrapper);
        Assert.notNull(userInfoDb, ResponseEnum.LOGIN_MOBILE_ERROR);

        //判断密码是否正确
        Assert.equals(MD5.encrypt(password),userInfoDb.getPassword(),ResponseEnum.LOGIN_PASSWORD_ERROR);

        //判断用户是否被锁定
        Assert.notEquals(userInfoDb.getStatus(), UserInfo.STATUS_LOCKED, ResponseEnum.LOGIN_LOKED_ERROR);

        //记录登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfoDb.getId());
        userLoginRecord.setIp(ip);
        userLoginRecordMapper.insert(userLoginRecord);

        //登录成功
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setMobile(userInfoDb.getMobile());
        userInfoVO.setHeadImg(userInfoDb.getHeadImg());
        userInfoVO.setName(userInfoDb.getName());
        userInfoVO.setNickName(userInfoDb.getNickName());
        userInfoVO.setUserType(userType);
        //生成token并给userinfovo赋值
        String token = JwtUtils.createToken(userInfoDb.getId(), userInfoDb.getName());
        userInfoVO.setToken(token);

        return userInfoVO;
    }

    @Override
    public IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery) {
        if(userInfoQuery==null){
            return baseMapper.selectPage(pageParam,null);
        }
        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq(!StringUtils.isEmpty(mobile),"mobile",mobile)
                            .eq(status!=null,"status",status)
                            .eq(userType!=null,"user_type",userType);
        return baseMapper.selectPage(pageParam,userInfoQueryWrapper);
    }

    @Override
    public void lock(Long id, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);
    }

    @Override
    public boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("mobile",mobile);
        Integer integer = baseMapper.selectCount(userInfoQueryWrapper);
        return integer<=0;
    }
}
