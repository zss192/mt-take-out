package com.sky.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
public class BloomFilterUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private BloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        // 初始化布隆过滤器，设置预期插入的元素数量和期望的误判率
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")), 1000, 0.01);

    }

    // 判断某个值是否存在于布隆过滤器中
    public boolean mightContain(String value) {
        return bloomFilter.mightContain(value);
    }

    // 将值添加到布隆过滤器中
    public void put(String value) {
        bloomFilter.put(value);
    }

}
