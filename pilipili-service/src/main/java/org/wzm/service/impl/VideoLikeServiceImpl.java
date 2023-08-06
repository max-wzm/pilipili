package org.wzm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.wzm.constant.RedisConstant;
import org.wzm.domain.VideoLike;
import org.wzm.mapper.VideoLikeMapper;
import org.wzm.service.VideoLikeService;
import org.wzm.service.VideoService;

public class VideoLikeServiceImpl implements VideoLikeService {
    @Autowired
    private VideoService                  videoService;
    @Autowired
    private VideoLikeMapper     videoLikeMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addVideoLike(Long videoId, Long userId) {
        String videoLikeKey = RedisConstant.VIDEO_LIKED_KEY + videoId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(videoLikeKey, userId.toString());
        if (Boolean.FALSE.equals(isMember)) {
            VideoLike videoLike = new VideoLike();
            videoLike.setUserId(userId);
            videoLike.setVideoId(videoId);
            videoLikeMapper.save(videoLike);
            stringRedisTemplate.opsForSet().add(videoLikeKey, userId.toString());
            stringRedisTemplate.opsForZSet().incrementScore(RedisConstant.VIDEO_LIKE_RANK, String.valueOf(videoId), 1);
        } else {
            videoLikeMapper.delete(userId, videoId);
            stringRedisTemplate.opsForSet().remove(videoLikeKey, userId.toString());
            stringRedisTemplate.opsForZSet().incrementScore(RedisConstant.VIDEO_LIKE_RANK, String.valueOf(videoId), -1);
        }
    }

    @Override
    public Long getVideoLike(Long videoId) {
        return (long) stringRedisTemplate.opsForZSet().score(RedisConstant.VIDEO_LIKE_RANK, videoId).doubleValue();
    }

}
