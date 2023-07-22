package org.wzm.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.wzm.constant.FollowingGroupConstant;
import org.wzm.domain.*;
import org.wzm.mapper.UserFollowingMapper;
import org.wzm.user.FollowingGroupService;
import org.wzm.user.FollowService;
import org.wzm.user.UserInfoService;
import org.wzm.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private TransactionTemplate   transactionTemplate;
    @Autowired
    private FollowingGroupService followingGroupService;
    @Autowired
    private UserService           userService;
    @Autowired
    private UserInfoService       userInfoService;
    @Autowired
    private UserFollowingMapper   userFollowingMapper;

    @Override
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (Objects.isNull(groupId)) {
            FollowingGroup fg = followingGroupService.getByUserIdAndType(userFollowing.getUserId(),
                                                                         FollowingGroupConstant.DEFAULT_FOLLOWING_GROUP);
            userFollowing.setGroupId(fg.getId());
        }
        Long followingId = userFollowing.getFollowingId();
        User followed = userService.getUser(followingId);
        if (Objects.isNull(followed)) {
            throw new BizException("Nonexistent user");
        }
        UserFollowing reverseFollowing = userFollowingMapper.getByDualIds(followingId, userFollowing.getUserId());
        if (Objects.nonNull(reverseFollowing)) {
            reverseFollowing.setMutual(true);
            userFollowing.setMutual(true);
            userFollowingMapper.deleteByDualIds(reverseFollowing.getUserId(), reverseFollowing.getFollowingId());
            userFollowingMapper.save(reverseFollowing);
        }
        userFollowingMapper.deleteByDualIds(userFollowing.getUserId(), userFollowing.getFollowingId());
        userFollowingMapper.save(userFollowing);
    }

    @Override
    public FollowingGroup getFollowingGroup(Long userId, Long groupId) {
        List<UserFollowing> userFollowings = userFollowingMapper.listByFollowerId(userId, groupId);
        FollowingGroup followingGroup = followingGroupService.getById(groupId);
        List<Long> followingIds = userFollowings.stream()
                .map(UserFollowing::getFollowingId)
                .collect(Collectors.toList());
        List<UserInfo> followingUserInfos = userInfoService.listByUserIds(followingIds);
        followingGroup.setFollowingUserInfoList(followingUserInfos);
        return followingGroup;
    }

    @Override
    public FollowingGroup getAllFollowings(Long userId) {
        List<UserFollowing> userFollowings = userFollowingMapper.listByFollowerId(userId, null);
        FollowingGroup allFollowingGroup = new FollowingGroup();
        allFollowingGroup.setUserId(userId);
        List<Long> followingIds = userFollowings.stream()
                .map(UserFollowing::getFollowingId)
                .collect(Collectors.toList());
        List<UserInfo> followingUserInfos = userInfoService.listByUserIds(followingIds);
        allFollowingGroup.setFollowingUserInfoList(followingUserInfos);
        allFollowingGroup.setName(FollowingGroupConstant.ALL_FOLLOWING_GROUP_NAME);
        return allFollowingGroup;
    }

    @Override
    public List<UserFollowing> getUserFans(Long userId) {
        List<UserFollowing> userFollowings = userFollowingMapper.listByFollowingId(userId);
        List<Long> followerIds = userFollowings.stream().map(UserFollowing::getUserId).collect(Collectors.toList());
        List<UserInfo> followerUserInfos = userInfoService.listByUserIds(followerIds);
        Map<Long, UserInfo> id2InfoMap = followerUserInfos.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, Function.identity()));
        userFollowings.forEach(uf -> {
            uf.setUserInfo(id2InfoMap.get(uf.getUserId()));
        });
        return userFollowings;
    }

    @Override
    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setType(FollowingGroupConstant.SELF_DEFINED_FOLLOWING_GROUP);
        followingGroupService.save(followingGroup);
        return followingGroup.getId();
    }
}
