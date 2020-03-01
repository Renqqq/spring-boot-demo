package com.springboot.demo.springbootdemo.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class ExpiredRedisKeyListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {

    }
}
