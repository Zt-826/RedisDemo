package com.example.redisdemo.service;

import com.example.redisdemo.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final String DELAY_QUEUE = "delay_queue";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            RBlockingDeque<User> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE);
            // 必须调用getDelayedQueue方法，否则获取不到已过期数据
            redissonClient.getDelayedQueue(blockingDeque);
            // 循环获取
            while (true) {
                try {
                    User user = blockingDeque.take();
                    log.info("Take user from delayed queue {}:{} at {}", user.getId(), user.getName(),
                            LocalDateTime.now());
                } catch (InterruptedException e) {
                    log.error("Failed to take from blocking deque caused by {}", e.getMessage());
                }
            }
        });
    }
}
