package com.springboot.demo.springbootdemo.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public interface IDistributedLockTaskAop {

    String POINT_CUT = "@annotation(com.springboot.demo.springbootdemo.aop.DistributedLock)";

    /**
     * 锁的环绕通知
     *
     * @param joinPoint join point
     * @return obj
     */
    Object aroundTaskLock(ProceedingJoinPoint joinPoint);
}
