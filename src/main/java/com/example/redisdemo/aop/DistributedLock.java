package com.example.redisdemo.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String lockName() default "";

    int expire() default 0;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
