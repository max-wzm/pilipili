package org.wzm.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.wzm.constant.RedisConstant;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserFollowing;
import org.wzm.domain.UserInfo;
import org.wzm.domain.UserMoments;
import org.wzm.mapper.UserMomentsMapper;
import org.wzm.user.FollowService;
import org.wzm.user.UserMomentsService;
import org.wzm.user.UserService;
import org.wzm.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMomentsServiceImpl implements UserMomentsService {
    @Autowired
    private UserMomentsMapper                  userMomentsMapper;
    @Autowired
    private RedisTemplate<String, UserMoments> redisTemplate;
    @Autowired
    private FollowService                      followService;
    @Autowired
    private UserService                        userService;

    @Override
    public void addUserMoments(UserMoments userMoments) throws Exception {
        userMomentsMapper.save(userMoments);
        String pubKey = RedisConstant.PUBLISHED_MOMENTS + userMoments.getUserId();
        redisTemplate.opsForZSet().add(pubKey, userMoments, DateUtil.getCurrentTimestamp());
        List<Long> fanIds = followService.getUserFans(userMoments.getUserId())
                .stream()
                .map(UserFollowing::getUserId)
                .collect(Collectors.toList());
        // todo 消息队列改造
        if (fanIds.size() < 10000) {
            fanIds.forEach(fanId -> {
                String key = RedisConstant.SUBSCRIBED_MOMENTS + fanId;
                redisTemplate.opsForZSet().add(key, userMoments, DateUtil.getCurrentTimestamp());
            });
        } else {
            List<Long> activeFanIds = fanIds.stream().filter(userService::isActive).collect(Collectors.toList());
            activeFanIds.forEach(fanId -> {
                String key = RedisConstant.SUBSCRIBED_MOMENTS + fanId;
                redisTemplate.opsForZSet().add(key, userMoments, DateUtil.getCurrentTimestamp());
            });
        }
    }

    @Override
    public List<UserMoments> getUserSubscribedMoments(Long userId) {
        String key = "subscribed_moments_" + userId;
        Set<UserMoments> userMoments = redisTemplate.opsForZSet().reverseRangeByScore(key, 0, Double.MAX_VALUE);
        return new ArrayList<>(userMoments);
    }

    @Override
    public PageResult<UserMoments> pageUserSubscribedMoments(Long userId, Double max, Integer offset, Integer count) {
        if (Objects.isNull(max) && !userService.isActive(userId)) {
            List<Long> followingIds = followService.getAllFollowings(userId)
                    .getFollowingUserInfoList()
                    .stream()
                    .map(UserInfo::getUserId)
                    .collect(Collectors.toList());
            followingIds.forEach(id -> {
                String pubKe = RedisConstant.PUBLISHED_MOMENTS + id;
                Set<ZSetOperations.TypedTuple<UserMoments>> typedTuples = redisTemplate.opsForZSet()
                        .rangeWithScores(pubKe, 0, -1);
                typedTuples.forEach(tuple -> {
                    String subKey = RedisConstant.SUBSCRIBED_MOMENTS + userId;
                    redisTemplate.opsForZSet().add(subKey, tuple.getValue(), tuple.getScore());
                });
            });
        }
        max = Objects.isNull(max) ? Integer.MAX_VALUE : max;
        offset = Objects.isNull(offset) ? 0 : offset;
        String key = RedisConstant.SUBSCRIBED_MOMENTS + userId;
        Set<ZSetOperations.TypedTuple<UserMoments>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, count);
        List<UserMoments> userMoments = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
        List<Double> scores = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getScore)
                .collect(Collectors.toList());
        double minScore = scores.get(scores.size() - 1);
        int os = (int) scores.stream().filter(s -> s == minScore).count();
        PageResult<UserMoments> pr = new PageResult<>();
        pr.setList(userMoments);
        pr.setMax(minScore);
        pr.setOffset(os);
        pr.setTotal(scores.size());
        Long cnt = redisTemplate.opsForZSet().count(key, 0, max);
        if (cnt < 1000) {
            List<UserMoments> page = userMomentsMapper.page(userMoments.size(), 1000);
            page.forEach(um -> redisTemplate.opsForZSet().add(key, um, um.getCreateTime().getTime()));
        }
        return pr;
    }
}
