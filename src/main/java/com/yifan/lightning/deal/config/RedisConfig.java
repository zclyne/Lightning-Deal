package com.yifan.lightning.deal.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

// 在pom.xml中加上session-redis依赖后，spring boot就会自动使用redis来存放session
// 加上@EnableRedisHttpSession
@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {
}
