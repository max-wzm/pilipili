package org.wzm.user;

import org.wzm.domain.UserInfo;

public interface UserInfoService {
    UserInfo getUserInfoByUserId(Long userId);

    void save(UserInfo userInfo);

    int updateUserInfos(UserInfo userInfo);
}
