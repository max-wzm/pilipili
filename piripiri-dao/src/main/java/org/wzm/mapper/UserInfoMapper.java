package org.wzm.mapper;

import org.wzm.domain.UserInfo;

public interface UserInfoMapper {
    UserInfo getUserInfoByUserId(Long userId);
}
