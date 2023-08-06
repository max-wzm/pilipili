package org.wzm.mapper;

import org.wzm.domain.PageResult;
import org.wzm.domain.UserFollowing;

import java.util.List;

public interface UserFollowingMapper {
    int deleteByDualIds(Long followerId, Long followingId);

    int save(UserFollowing userFollowing);

    List<UserFollowing> listByFollowerId(Long followerId, Long groupId);

    List<UserFollowing> listByFollowingId(Long followingId);

    UserFollowing getByDualIds(Long followerId, Long followingId);

    List<UserFollowing> page(int offset, int pageSize);
}
