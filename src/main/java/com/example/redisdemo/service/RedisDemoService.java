package com.example.redisdemo.service;

import com.example.redisdemo.aop.DistributedLock;
import com.example.redisdemo.bean.User;
import com.example.redisdemo.mapper.RedisDemoMapper;
import com.example.redisdemo.utils.ExpireUtil;
import com.example.redisdemo.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisDemoService {
    @Resource
    private RedisDemoMapper mapper;

    /**
     * 缓存服务
     */
    @Resource
    private CacheService cacheService;

    /**
     * 用来实现分布式锁
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 新增用户
     *
     * @param user user
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(User user) {
        // 向数据库中进行写入
        mapper.addUser(user);

        // 写入缓存
        log.info("开始写入缓存: {}", user.getId());
        cacheService.set(user.getId(), GsonUtil.toJson(user), 30, TimeUnit.SECONDS);
    }

    /**
     * 返回用户
     *
     * @param userId userId
     * @return User
     */
    public User getUser(String userId) {
        // 先从缓存中读取
        Object value = cacheService.get(userId);
        if (value != null) {
            // 如果缓存中存在
            log.info("缓存命中: {}", userId);
            if (Objects.equals(value, "NULL")) {
                // 说明数据库中不存在该用户
                log.info("缓存中为null");
                return null;
            } else {
                // 存在则返回该用户
                return GsonUtil.toObject(value, User.class);
            }
        } else {
            // 缓存中不存在，查询数据库，并写入缓存
            log.info("缓存未命中: {}，查询数据库", userId);
            User user = mapper.getUser(userId);
            if (user == null) {
                // 数据库中没有，为避免缓存穿透，缓存写入null
                log.info("为避免缓存穿透，缓存写入null");
                cacheService.set(userId, "NULL", 30, TimeUnit.SECONDS);
                return null;
            } else {
                // 数据库中存在该用户
                log.info("开始写入缓存: {}", user.getId());
                // 为避免缓存击穿，采用随机过期时间进行缓存
                cacheService.set(userId, GsonUtil.toJson(user), ExpireUtil.getExpireTime(), TimeUnit.SECONDS);
                return user;
            }
        }
    }

    /**
     * 库存扣减
     *
     * @param goodId goodId
     */
    public void deleteCount(String goodId) {
        // 使用UUID进行加锁，防止误解锁
        String uuid = UUID.randomUUID().toString();
        log.info("UUID is: {}", uuid);
        RLock lock = redissonClient.getLock(uuid);
        try {
            // 加锁，没拿到锁则进行自旋
            // redisson默认加锁时长为30s，每10s进行一次续期
            lock.lock();
            mapper.deleteCount(goodId);
        } finally {
            lock.unlock();
        }
    }

    @DistributedLock(lockName = "deleteCountLock", expire = 5, timeUnit = TimeUnit.MINUTES)
    public void deleteCountWithAnnotation(String goodId) {
        mapper.deleteCount(goodId);
    }
}
