package com.springboot.demo.springbootdemo.task;

import com.springboot.demo.springbootdemo.aop.DistributedLock;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DistributedTask {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DistributedTask.class);

    /**
     * 测试 redis 加锁之后的分布式定时任务是否可以在单个机器上运行
     */
    @DistributedLock(expiredSecond = 35)
    @Scheduled(cron = "0 * * * * ? ")
    public void testRedisLockTask() {
        // 每分钟运行一次
        log.info("distributed task start running !");

        for (int i = 0; i < 10; i++) {
            log.info("do something which id is [{}]", i);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("do something which id is [{}] error!", i);
            }
            log.info("do something which id is [{}] succeed!", i);
        }

        log.info("distributed task end running!");
    }
}
