package com.example.redisdemo.controller;

import com.example.redisdemo.aop.DistributedLock;
import com.example.redisdemo.bean.User;
import com.example.redisdemo.service.RedisDemoService;
import com.example.redisdemo.utils.ExpireUtil;
import com.example.redisdemo.utils.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class RedisDemoController {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisDemoService redisDemoService;

    /**
     * 新增用户
     *
     * @param user user
     */
    @RequestMapping("/addUser")
    public void addUser(@RequestBody User user) {
        // 向数据库中进行写入
        redisDemoService.addUser(user);

        // 写入缓存
        log.info("开始写入缓存: {}", user.getId());
        RBucket<Object> bucket = redissonClient.getBucket(user.getId());
        bucket.set(GsonUtil.toJson(user), 30, TimeUnit.SECONDS);
    }


    /**
     * 获取用户
     *
     * @param userId userId
     * @return User
     */
    @RequestMapping("/getUser/{userId}")
    public User getUser(@PathVariable String userId) {
        // 先从缓存中读取
        RBucket<Object> bucket = redissonClient.getBucket(userId);
        if (bucket.isExists()) {
            // 如果缓存中存在
            log.info("缓存命中: {}", userId);
            String user = (String) bucket.get();
            if (Objects.equals(user, "NULL")) {
                // 说明数据库中不存在该用户
                log.info("缓存中为null");
                return null;
            } else {
                // 存在则返回该用户
                return GsonUtil.toObject((String) bucket.get(), User.class);
            }
        } else {
            // 缓存中不存在，查询数据库，并写入缓存
            log.info("缓存未命中: {}，查询数据库", userId);
            User user = redisDemoService.getUser(userId);
            if (user == null) {
                // 数据库中没有，为避免缓存穿透，缓存写入null
                log.info("为避免缓存穿透，缓存写入null");
                bucket.set("NULL", 30, TimeUnit.SECONDS);
                return null;
            } else {
                // 数据库中存在该用户
                log.info("开始写入缓存: {}", user.getId());
                // 为避免缓存击穿，采用随机过期时间进行缓存
                bucket.set(GsonUtil.toJson(user), ExpireUtil.getExpireTime(), TimeUnit.SECONDS);
                return user;
            }
        }
    }

    /**
     * 测试分布式锁，删除库中物品个数
     *
     * @param goodId goodId
     */
    @RequestMapping("/deleteCount/{goodId}")
    public void deleteCount(@PathVariable String goodId) {
        // 使用UUID进行加锁，防止误解锁
        String uuid = UUID.randomUUID().toString();
        log.info("UUID : {}", uuid);
        RLock lock = redissonClient.getLock(uuid);
        try {
            // 加锁，没拿到锁则进行自旋
            // redisson默认加锁时长为30s，每10s进行一次续期
            lock.lock();
            redisDemoService.deleteCount(goodId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 使用注解测试分布式锁，删除库中物品个数
     *
     * @param goodId goodId
     */
    @DistributedLock(lockName = "deleteCountLock", expire = 5, timeUnit = TimeUnit.MINUTES)
    @RequestMapping("/deleteCountWithAnnotation/{goodId}")
    public void deleteCountWithAnnotation(@PathVariable String goodId) {
        redisDemoService.deleteCount(goodId);
    }
}
