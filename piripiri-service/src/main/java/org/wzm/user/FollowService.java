package org.wzm.user;

import org.wzm.domain.FollowingGroup;
import org.wzm.domain.UserFollowing;

import java.util.List;

public interface FollowService {
    void addUserFollowings(UserFollowing userFollowing);

    //获取用户关注列表
    //查询关注用户的基本信息
    //将关注用户按分组进行分类
    FollowingGroup getFollowingGroup(Long userId, Long groupId);

    FollowingGroup getAllFollowings(Long userId);

    List<UserFollowing> getUserFans(Long userId);

    Long addUserFollowingGroups(FollowingGroup followingGroup);
}
