package org.wzm.mapper;

import org.apache.ibatis.annotations.Param;
import org.wzm.domain.User;

public interface UserMapper {
    User getUserById(@Param("userId") Long userId);

    User getUserByPhone(@Param("phone") String phone);

    int save(@Param("user") User user);
}
