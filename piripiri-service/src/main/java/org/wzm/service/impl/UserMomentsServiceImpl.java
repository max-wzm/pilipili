package org.wzm.service.impl;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.wzm.constant.RedisConstant;
import org.wzm.constant.RocketMQConstant;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserInfo;
import org.wzm.domain.UserMoments;
import org.wzm.mapper.UserMomentsMapper;
import org.wzm.service.FollowService;
import org.wzm.service.UserMomentsService;
import org.wzm.service.UserService;
import org.wzm.utils.DateUtil;
import org.wzm.utils.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMomentsServiceImpl implements UserMomentsService {
    @Autowired
    private UserMomentsMapper                  userMomentsMapper;
    @Autowired
    private RedisTemplate<String, UserMoments> userMomentsRedisTemplate;
    @Autowired
    private FollowService                      followService;
    @Autowired
    private UserService                        userService;
    @Autowired
    private DefaultMQProducer                  momentsProducer;

    @Override
    public void addUserMoments(UserMoments userMoments) throws Exception {
        userMomentsMapper.save(userMoments);
        String pubKey = RedisConstant.PUBLISHED_MOMENTS + userMoments.getUserId();
        userMomentsRedisTemplate.opsForZSet().add(pubKey, userMoments, DateUtil.getCurrentTimestamp());
        Message rocketMessage = new Message(RocketMQConstant.MOMENTS_TOPIC,
                                            JsonUtil.toJson(userMoments).getBytes(StandardCharsets.UTF_8));
        momentsProducer.send(rocketMessage);
    }

    @Override
    public List<UserMoments> getUserSubscribedMoments(Long userId) {
        String key = "subscribed_moments_" + userId;
        Set<UserMoments> userMoments = userMomentsRedisTemplate.opsForZSet().reverseRangeByScore(key, 0, Double.MAX_VALUE);
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
                Set<ZSetOperations.TypedTuple<UserMoments>> typedTuples = userMomentsRedisTemplate.opsForZSet()
                        .rangeWithScores(pubKe, 0, -1);
                typedTuples.forEach(tuple -> {
                    String subKey = RedisConstant.SUBSCRIBED_MOMENTS + userId;
                    userMomentsRedisTemplate.opsForZSet().add(subKey, tuple.getValue(), tuple.getScore());
                });
            });
        }
        max = Objects.isNull(max) ? Double.MAX_VALUE : max;
        offset = Objects.isNull(offset) ? 0 : offset;
        String key = RedisConstant.SUBSCRIBED_MOMENTS + userId;
        Set<ZSetOperations.TypedTuple<UserMoments>> typedTuples = userMomentsRedisTemplate.opsForZSet()
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
        Long cnt = userMomentsRedisTemplate.opsForZSet().count(key, 0, max);
        if (cnt < 1000) {
            List<UserMoments> page = userMomentsMapper.page(userMoments.size(), 1000);
            page.forEach(um -> userMomentsRedisTemplate.opsForZSet().add(key, um, um.getCreateTime().getTime()));
        }
        return pr;
    }
}
