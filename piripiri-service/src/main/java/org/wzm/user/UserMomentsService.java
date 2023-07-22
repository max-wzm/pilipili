package org.wzm.user;

import org.wzm.domain.PageResult;
import org.wzm.domain.RedisPageMomentsDTO;
import org.wzm.domain.UserMoments;

import java.util.List;

public interface UserMomentsService {
    void addUserMoments(UserMoments userMoments) throws Exception;

    List<UserMoments> getUserSubscribedMoments(Long userId);

    PageResult<UserMoments> pageUserSubscribedMoments(Long userId, Double max, Integer offset, Integer count);
}
