package com.springboot.demo.springbootdemo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RedisTemplateService implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(RedisTemplateService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String RELEASE_SCRIPT = "if redis.call('get',KEYS[1])== ARGV[1] " +
            "then return redis.call('del',KEYS[1]) else return 0 end";

    private static final Long RELEASE_SUC = 1L;

    /**
     * 使用redis template 加锁
     *
     * @param taskName       任务名称
     * @param ip             ip地址
     * @param expiredSeconds 过期时间
     * @return boolean
     */
    public boolean distributedLockTask(String taskName, String ip, long expiredSeconds) {
        try {
            Boolean exeRes = redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.set(taskName.getBytes(), ip.getBytes(), Expiration.milliseconds(expiredSeconds),
                            RedisStringCommands.SetOption.ifAbsent()));

            return exeRes != null && exeRes;
        } catch (Exception e) {
            logger.error("redis 异常", e);
            return true;
        }
    }

    /**
     * 使用 redis template 释放锁
     *
     * @param taskName 任务名称
     * @param ip       ip地址
     * @return boolean
     */
    public boolean distributedReleaseTask(String taskName, String ip) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(RELEASE_SCRIPT, Long.class);
            Long res = redisTemplate.execute(script, Collections.singletonList(taskName), ip);
            logger.info("release result:{}", res);
            return RELEASE_SUC.equals(res);
        } catch (Exception e) {
            logger.error("redis 异常！", e);
            return true;
        }
    }

    public String get(String taskName) {
        return redisTemplate.opsForValue().get(taskName);
    }

    /**
     * redis消息监听
     *
     * @param message message
     * @param pattern pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        byte[] channel = message.getChannel();
        RedisSerializer<?> valueSerializer = redisTemplate.getValueSerializer();
        String msg = (String) valueSerializer.deserialize(body);
        String ch = (String) valueSerializer.deserialize(channel);
        logger.info("===> msg: {} ===> channel: {} ===> message parttern: {}",
                msg, ch, new String(pattern));
    }
}
