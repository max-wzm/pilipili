package org.wzm.mapper;

import org.wzm.domain.UserMoments;

import java.util.List;

public interface UserMomentsMapper {
    int save(UserMoments userMoments);

    UserMoments getById(Long id);

    List<UserMoments> page(int offset, int pageSize);
}
