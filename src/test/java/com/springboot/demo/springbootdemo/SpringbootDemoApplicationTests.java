package com.springboot.demo.springbootdemo;

import com.springboot.demo.springbootdemo.service.JedisService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootDemoApplicationTests {

    private Logger log = LoggerFactory.getLogger(SpringbootDemoApplicationTests.class);

    @Autowired
    private JedisService jedisService;

    @Test
    public void testJedisConnection() {
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
