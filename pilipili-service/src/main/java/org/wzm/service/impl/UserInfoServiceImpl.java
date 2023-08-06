package org.wzm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.domain.UserInfo;
import org.wzm.mapper.UserInfoMapper;
import org.wzm.service.UserInfoService;

import java.util.List;

public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        return userInfoMapper.getUserInfoByUserId(userId);
    }

    @Override
    public List<UserInfo> listByUserIds(List<Long> userIds) {
        return userInfoMapper.listByUserIds(userIds);
    }

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.save(userInfo);
    }

    @Override
    public int updateUserInfos(UserInfo userInfo) {
        return userInfoMapper.updateUserInfos(userInfo);
    }
}
