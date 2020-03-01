package com.springboot.demo.springbootdemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

@Service
@ConfigurationProperties("redis.jedis")
public class JedisService {

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    private String host;

    private Logger log = LoggerFactory.getLogger(JedisService.class);

    private static final String LOCK_SUC = "OK";

    private static final Integer RELEASE_SUC = 1;

    private static final String RELEASE_SCRIPT = "if redis.call('get',KEYS[1])== ARGV[1] " +
            "then return redis.call('del',KEYS[1]) else return 0 end";

//    @Autowired
    private JedisPool pool;

    public Jedis connection() {
        return pool.getResource();
    }

    public String set(String key, String val) {
        Jedis jedis = null;
        try {
            jedis = connection();
            String status = jedis.set(key, val);
            return status;
        } finally {
            assert jedis != null;
            jedis.close();
        }
    }

    /**
     * 锁定时任务， 让某一个机器获得执行定时任务的权限
     *
     * @param taskName       定时任务名称
     * @param ip             机器IP地址
     * @param expiredSeconds 超时秒数
     * @return boolean
     */
    public boolean distributedLockTask(Jedis jedis, String taskName, String ip, int expiredSeconds) {
        if (jedis == null) return false;
        try {
            SetParams params = new SetParams();
            params.nx();
            params.ex(expiredSeconds);
            String lockStatus = jedis.set(taskName, ip, params);
            log.info("lock result: {}", lockStatus);
            return LOCK_SUC.equals(lockStatus);
        } finally {
            jedis.close();
        }
    }

    /**
     * 释放分布式锁
     *
     * @param jedis    jedis
     * @param taskName 任务名
     * @param ip       ip
     * @return boolean
     */
    public boolean distributedReleaseTask(Jedis jedis, String taskName, String ip) {
        if (jedis == null) return false;
        Object eval = jedis.eval(RELEASE_SCRIPT, Collections.singletonList(taskName), Collections.singletonList(ip));
        boolean res = RELEASE_SUC.equals(eval);
        log.info("release result: {}", res);
        return res;
    }

    public String get(String name) {
        Jedis connection = connection();
        return connection.get(name);
    }
}
