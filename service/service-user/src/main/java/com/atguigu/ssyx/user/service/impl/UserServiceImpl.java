package com.atguigu.ssyx.user.service.impl;

import com.atguigu.ssyx.model.user.Leader;
import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.model.user.UserDelivery;
import com.atguigu.ssyx.user.mapper.LeaderMapper;
import com.atguigu.ssyx.user.mapper.UserDeliveryMapper;
import com.atguigu.ssyx.user.mapper.UserMapper;
import com.atguigu.ssyx.user.service.UserService;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserDeliveryMapper userDeliveryMapper;

    @Resource
    private LeaderMapper leaderMapper;

    @Override
    public User getUserByOpenId(String openId) {
        return baseMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getOpenId,openId));
    }

    @Override
    public LeaderAddressVo getLeaderAddressByUserId(Long userId) {
        UserDelivery userDelivery = userDeliveryMapper.selectOne(Wrappers.<UserDelivery>lambdaQuery()
                        .eq(UserDelivery::getIsDefault,1)
                        .eq(UserDelivery::getUserId, userId));
        if(userDelivery == null){
            return null;
        }
        Leader leader = leaderMapper.selectById(userDelivery.getLeaderId());

        LeaderAddressVo leaderAddressVo = new LeaderAddressVo();
        BeanUtils.copyProperties(leader,leaderAddressVo);
        leaderAddressVo.setUserId(userId);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());
        return leaderAddressVo;
    }

    @Override
    public UserLoginVo getUserLoginVo(Long id) {
        User user = baseMapper.selectById(id);
        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setUserId(id);
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setOpenId(user.getOpenId());
        userLoginVo.setIsNew(user.getIsNew());

        UserDelivery userDelivery = userDeliveryMapper.selectOne(Wrappers.<UserDelivery>lambdaQuery()
                .eq(UserDelivery::getIsDefault,1)
                .eq(UserDelivery::getUserId, id));
        if(userDelivery != null){
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        }
        return userLoginVo;
    }
}
