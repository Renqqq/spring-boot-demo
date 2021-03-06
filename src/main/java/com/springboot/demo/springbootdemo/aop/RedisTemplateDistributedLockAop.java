package com.springboot.demo.springbootdemo.aop;

import com.springboot.demo.springbootdemo.service.RedisTemplateService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Aspect
@Component
public class RedisTemplateDistributedLockAop implements IDistributedLockAop {

    Logger log = LoggerFactory.getLogger(RedisTemplateDistributedLockAop.class);

    @Autowired
    private RedisTemplateService redisTemplateService;

    @Override
    @Around(POINT_CUT)
    public Object aroundTaskLock(ProceedingJoinPoint joinPoint) throws Exception {

        // 在定时任务开始之前通过aop设置分布式锁,保证只有一个机器在运行定时任务
        // key: 任务名称  --> value：执行机器ip
        // 在开始之前通过redis设置key对应的值， 设置上的机器才能继续运行
        // 值要设置合理的过期时间，保证在下一次定时任务开始之前值已经过期
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSig.getName();
        String className = methodSig.getMethod().getDeclaringClass().getSimpleName();
        DistributedLock lockAnno = methodSig.getMethod().getAnnotation(DistributedLock.class);
        long expiredTime = lockAnno.expiredSecond();
        boolean manualRelease = lockAnno.manualRelease();
        String currentIpAddress = "";
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            currentIpAddress = InetAddress.getByName(hostName).getHostAddress();
            log.info("获取机器ip:[{}]", currentIpAddress);
        } catch (UnknownHostException e) {
            log.error("未知的机器ip, 使用随机数", e);
            currentIpAddress = UUID.randomUUID().toString();
        }
        String taskName = className + "." + methodName;
        log.info("执行定时任务名：{}", taskName);
        if (redisTemplateService.distributedLockTask(taskName, currentIpAddress, expiredTime)) {
            // 加锁成功
            log.info("定时任务{}在当前机器[{}]上运行！", taskName, currentIpAddress);
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                log.error("定时任务执行失败!", e);
                throw e;
            } catch (Throwable throwable) {
                log.error("定时任务执行失败!", throwable);
            } finally {
                // 释放锁
                if (manualRelease) {
                    // 手动释放锁，不手动释放的话会根据过期时间自动释放
                    boolean release = redisTemplateService.distributedReleaseTask(taskName, currentIpAddress);
                    if (release) {
                        log.info("分布式锁在{}上已释放", currentIpAddress);
                    }
                }
            }
        } else {
            log.info("定时任务{}在机器[{}]上运行！", taskName, redisTemplateService.get(taskName));
        }
        return null;
    }
}
