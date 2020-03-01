package com.springboot.demo.springbootdemo.controller;

import com.springboot.demo.springbootdemo.service.JedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    private Logger log = LoggerFactory.getLogger(RedisController.class);

    @Autowired
    private JedisService jedisService;

    @GetMapping("redis")
    private void testRedis() {
        String status = jedisService.set("name", "renqing");
        log.info("创建name结果：{}", status);
        status = jedisService.set("name1", "wangxianyu");
        log.info("创建name1结果：{}", status);

        String name = jedisService.get("name");
        log.info("获取name结果：{}", name);
        String name1 = jedisService.get("name1");
        log.info("获取name1结果：{}", name1);
    }
}
