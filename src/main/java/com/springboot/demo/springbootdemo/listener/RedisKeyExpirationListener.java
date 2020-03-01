package com.springboot.demo.springbootdemo.listener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private Logger log = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("key: {} 已过期！", expiredKey);
    }
}
