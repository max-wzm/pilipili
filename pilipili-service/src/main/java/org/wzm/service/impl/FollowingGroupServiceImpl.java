package org.wzm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.domain.FollowingGroup;
import org.wzm.mapper.FollowingGroupMapper;
import org.wzm.service.FollowingGroupService;

import java.util.List;

public class FollowingGroupServiceImpl implements FollowingGroupService {
    @Autowired
    private FollowingGroupMapper followingGroupMapper;

    @Override
    public FollowingGroup getByUserIdAndType(Long userId, String type) {
        return followingGroupMapper.getByUserIdAndType(userId, type);
    }

    @Override
    public FollowingGroup getById(Long id) {
        return followingGroupMapper.getById(id);
    }

    @Override
    public void save(FollowingGroup followingGroup) {
        followingGroupMapper.save(followingGroup);
    }

    @Override
    public List<FollowingGroup> listByUserId(Long userId) {
        return followingGroupMapper.listByUserId(userId);
    }
}
