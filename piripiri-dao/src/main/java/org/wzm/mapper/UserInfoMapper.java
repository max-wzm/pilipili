package org.wzm.mapper;

import org.wzm.domain.UserInfo;

import java.util.List;

public interface UserInfoMapper {
    UserInfo getUserInfoByUserId(Long userId);

    List<UserInfo> listByUserIds(List<Long> userIds);

    void save(UserInfo userInfo);

    int updateUserInfos(UserInfo userInfo);
}
