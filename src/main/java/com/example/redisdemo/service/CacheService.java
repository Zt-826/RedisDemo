package com.example.redisdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheService {

    /**
     * 用来实现缓存读写
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取key
     *
     * @param keys keys
     * @return Set
     */
    public Set<String> keys(String keys) {
        return redisTemplate.keys(keys);
    }

    /**
     * 判断key是否存在
     *
     * @param key key
     * @return boolean
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置缓存
     *
     * @param key   key
     * @param value value
     * @return boolean
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Failed to set cache to Redis caused by {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置带过期时间的缓存
     *
     * @param key      key
     * @param value    value
     * @param time     time
     * @param timeUnit timeUnit
     * @return boolean
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        if (time <= 0) {
            log.error("Expire time must be positive.");
            return false;
        }

        try {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
            return true;
        } catch (Exception e) {
            log.error("Failed to set cache to Redis caused by {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取key
     *
     * @param key key
     * @return Object
     */
    public Object get(String key) {
        if (key == null) {
            log.error("Key should not be null.");
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to set cache to Redis caused by {}", e.getMessage());
            return null;
        }
    }

    /**
     * 删除key
     *
     * @param key key
     * @return boolean
     */
    public boolean del(String... key) {
        if (key == null || key.length == 0) {
            log.error("Key should not be null or empty.");
            return false;
        }

        try {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to set cache to Redis caused by {}", e.getMessage());
            return false;
        }
    }


    /**
     * 指定缓存过期时间
     *
     * @param key      key
     * @param time     time
     * @param timeUnit timeUnit
     * @return boolean
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        if (time <= 0) {
            log.error("Expire time must be positive.");
            return false;
        }

        try {
            redisTemplate.expire(key, time, timeUnit);
            return true;
        } catch (Exception e) {
            log.error("Failed to set cache to Redis caused by {}", e.getMessage());
            return false;
        }
    }
}
