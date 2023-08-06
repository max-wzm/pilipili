package org.wzm.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.wzm.constant.RedisConstant;
import org.wzm.domain.BizException;
import org.wzm.domain.PageResult;
import org.wzm.domain.Video;
import org.wzm.mapper.VideoMapper;
import org.wzm.service.FastDFSService;
import org.wzm.service.RedisCacheService;
import org.wzm.service.VideoService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper       videoMapper;
    @Autowired
    private FastDFSService    fastDFSService;
    @Autowired
    private RedisCacheService redisCacheService;

    @PostConstruct
    public void init() {
        int pageIdx = 0;
        List<Video> videos;
        do {
            videos = videoMapper.page(pageIdx, 1000);
            pageIdx++;
            videos.forEach(video -> redisCacheService.putToVideoBloomFilter(video.getId()));
        } while (CollectionUtils.isNotEmpty(videos));
    }

    @Override
    @Transactional
    public void addVideos(Video video) {
        // 雪崩
        videoMapper.save(video);
        redisCacheService.setValueRand(RedisConstant.VIDEO_KEY + video.getId(), video, 30, TimeUnit.MINUTES);
    }

    @Override
    public PageResult<Video> pageListVideos(Integer pageIndex, Integer pageSize) {
        List<Video> page = videoMapper.page(pageIndex, pageSize);
        PageResult<Video> result = new PageResult<>();
        result.setList(page);
        result.setTotal(page.size());
        return result;
    }

    @Override
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) {
        fastDFSService.viewVideoOnlineBySlices(request, response, url);
    }

    @Override
    public Video getById(Long videoId) {
        // 缓存穿透
        if (!redisCacheService.mightContain(videoId)) {
            throw new BizException("Could not find video!");
        }
        String key = RedisConstant.VIDEO_KEY + videoId;
        Video video = redisCacheService.get(key, Video.class);
        if (Objects.nonNull(video)) {
            return video;
        }
        // 缓存击穿 重建缓存
        String lockKey = RedisConstant.VIDEO_LOCK_KEY + videoId;
        redisCacheService.lockRedis(lockKey);
        video = redisCacheService.get(key, Video.class);
        if (Objects.nonNull(video)) {
            redisCacheService.unlockRedis(lockKey);
            return video;
        }

        video = videoMapper.getById(videoId);
        if (Objects.isNull(video)) {
            redisCacheService.unlockRedis(lockKey);
            throw new BizException("Could not find video!");
        }
        redisCacheService.setValueRand(key, video, 30, TimeUnit.MINUTES);
        redisCacheService.unlockRedis(key);
        return video;
    }

    @Override
    public void update(Video video) {
        videoMapper.delete(video);
        redisCacheService.delete(RedisConstant.VIDEO_KEY + video.getId());
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void storeHotVideos() {
        List<Long> videoIds = redisCacheService.popMax(RedisConstant.VIDEO_LIKE_RANK, 1000)
                .stream()
                .map(t -> Long.valueOf(t.getValue()))
                .collect(Collectors.toList());
        List<Video> videos = videoMapper.listByIds(videoIds);
        videos.forEach(video -> {
            redisCacheService.setValueRand(RedisConstant.VIDEO_KEY + video.getId(), video, 1, TimeUnit.DAYS);
        });
    }

}
