package org.wzm.service;

import org.wzm.domain.UserInfo;

import java.util.List;

public interface UserInfoService {
    UserInfo getUserInfoByUserId(Long userId);

    List<UserInfo> listByUserIds(List<Long> userIds);

    void save(UserInfo userInfo);

    int updateUserInfos(UserInfo userInfo);
}
