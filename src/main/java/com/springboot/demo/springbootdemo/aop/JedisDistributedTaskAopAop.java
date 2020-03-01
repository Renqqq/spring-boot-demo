package com.springboot.demo.springbootdemo.aop;

import com.springboot.demo.springbootdemo.service.JedisService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Aspect
@Component
public class JedisDistributedTaskAopAop implements IDistributedLockTaskAop {

    private Logger log = LoggerFactory.getLogger(JedisDistributedTaskAopAop.class);

    @Autowired
    private JedisService jedisService;

    @Override
//    @Around(POINT_CUT)
    public Object aroundTaskLock(ProceedingJoinPoint joinPoint) {
        // 在定时任务开始之前通过aop设置分布式锁,保证只有一个机器在运行定时任务
        // key: 任务名称  --> value：执行机器ip
        // 在开始之前通过redis设置key对应的值， 设置上的机器才能继续运行
        // 值要设置合理的过期时间，保证在下一次定时任务开始之前值已经过期
        Jedis jedis = jedisService.connection();
        MethodSignature method = (MethodSignature) joinPoint.getSignature();
        String methodName = method.getName();
        String className = method.getMethod().getDeclaringClass().getSimpleName();
        String currentIpAddress = "";
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            currentIpAddress = InetAddress.getByName(hostName).getHostAddress();
            log.info("获取机器ip：{}", currentIpAddress);
        } catch (UnknownHostException e) {
            log.error("未知的机器ip", e);
        }
        String taskName = className + "." + methodName;
        DistributedLock lockAnno = method.getMethod().getAnnotation(DistributedLock.class);
        int expiredTime = lockAnno.expiredSecond();
        log.info("执行定时任务名：{}", taskName);
        if (jedisService.distributedLockTask(jedis, taskName, currentIpAddress, 35)) {
            // 加锁成功
            log.info("定时任务{}在当前机器{}上运行！", taskName, currentIpAddress);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                log.error("定时任务执行失败!", throwable);
            } finally {
                // 释放锁
                jedisService.distributedReleaseTask(jedis, taskName, currentIpAddress);
            }
        } else {
            log.info("定时任务{}在机器{}上运行！", taskName, jedis.get(taskName));
        }
        return null;
    }
}
