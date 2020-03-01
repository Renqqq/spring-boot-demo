package com.springboot.demo.springbootdemo.aop;

import java.lang.annotation.*;

/**
 * 分布式锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁过期时间， 秒级别
     *
     * @return int
     */
    long expiredSecond() default 1;

    /**
     * 是否手动释放
     *
     * @return bool
     */
    boolean manualRelease() default true;
}
