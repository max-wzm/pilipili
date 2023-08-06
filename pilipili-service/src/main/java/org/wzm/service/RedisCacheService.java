package org.wzm.service;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisCacheService {

    void putToVideoBloomFilter(Long id);

    boolean mightContain(Long id);

    boolean tryLock(String key);

    void lockRedis(String key);

    void unlockRedis(String key);

    void setValueRand(String key, Object value, long time, TimeUnit unit);

    <R> R get(String key, Class<R> clazz);

    void delete(String key);

    Set<ZSetOperations.TypedTuple<String>> popMax(String key, long count);
}
