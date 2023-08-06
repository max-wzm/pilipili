package org.wzm.service.impl;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.wzm.constant.RedisConstant;
import org.wzm.service.RedisCacheService;
import org.wzm.utils.JsonUtil;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private BloomFilter<Long>   videoBloomFilter;
    private Random              rand = new Random();

    @PostConstruct
    public void init() {
        videoBloomFilter = BloomFilter.create(Funnels.longFunnel(), 10000, 0.01);
    }

    @Override
    public void putToVideoBloomFilter(Long id) {
        videoBloomFilter.put(id);
    }

    @Override
    public boolean mightContain(Long id) {
        return videoBloomFilter.mightContain(id);
    }

    @Override
    public boolean tryLock(String key) {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", RedisConstant.LOCK_TTL, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public void lockRedis(String key) {
        boolean locked = tryLock(key);
        while (!locked) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            locked = tryLock(key);
        }
    }

    @Override
    public void unlockRedis(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public void setValueRand(String key, Object value, long time, TimeUnit unit) {
        int dev = rand.nextInt(50) - 50 / 2;
        time = time + (time * dev) / 100;
        stringRedisTemplate.opsForValue().set(key, JsonUtil.toJson(value), time, unit);
    }

    @Override
    public <R> R get(String key, Class<R> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        return JsonUtil.fromJson(json, clazz);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> popMax(String key, long count) {
        return stringRedisTemplate.opsForZSet().popMax(key, count);
    }

}
