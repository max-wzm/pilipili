package org.wzm.mapper;

import org.wzm.domain.FollowingGroup;

import java.util.List;

public interface FollowingGroupMapper {
    FollowingGroup getByUserIdAndType(Long userId, String type);

    FollowingGroup getById(Long id);

    void save(FollowingGroup followingGroup);

    List<FollowingGroup> listByUserId(Long userId);
}
