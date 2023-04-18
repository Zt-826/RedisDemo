package com.example.redisdemo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 分布式锁切面，使用注解的方式使用Redis分布式锁
 */
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.example.redisdemo.aop.DistributedLock)")
    public void pointCut() {
    }

    @Around("pointCut() && @annotation(distributedLock)")
    public Object distributedLockAspect(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        // 使用UUID进行加锁，防止误解锁
        String uuid = UUID.randomUUID().toString();
        RLock lock = redissonClient.getLock(distributedLock.lockName() + "-" + uuid);
        try {
            // 加锁，没拿到锁则进行自旋
            if (distributedLock.expire() != 0) {
                // 使用注解内设置的过期时间
                lock.lock(distributedLock.expire(), distributedLock.timeUnit());
            } else {
                // redisson默认加锁时长为30s，每10s进行一次续期
                lock.lock();
            }
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("Failed to get Distributed Lock caused by {}", throwable.getMessage());
        } finally {
            lock.unlock();
        }
        return null;
    }
}
