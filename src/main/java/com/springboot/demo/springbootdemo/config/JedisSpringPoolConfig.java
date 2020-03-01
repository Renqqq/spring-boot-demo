package com.springboot.demo.springbootdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisSpringPoolConfig {

    private Logger log = LoggerFactory.getLogger(JedisSpringPoolConfig.class);

    /**
     * jedis 连接池配置
     *
     * @return 连接池对象
     */
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        JedisPool pool = new JedisPool(config, "master");
        log.info("Redis连接成功!");
        return pool;
    }
}
