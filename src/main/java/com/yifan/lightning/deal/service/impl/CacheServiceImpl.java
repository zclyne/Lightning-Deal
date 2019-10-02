package com.yifan.lightning.deal.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yifan.lightning.deal.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String, Object> commonCache = null;

    // @PostConstruct是Spring提供的注解
    // 在bean加载完成后，Spring会自动执行这个方法
    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                // 设置缓存容器的初始容量为10个key-value pair
                .initialCapacity(10)
                // 设置缓存的最大容量为100个key-value pair，超过100个后会按照LRU策略移除缓存项目
                .maximumSize(100)
                // 设置写缓存后多少秒后过期，此处设置为60秒，本地缓存的过期时间应比redis短很多
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key); // 如果存在key，则返回对应value；如果不存在，则返回null
    }
}
