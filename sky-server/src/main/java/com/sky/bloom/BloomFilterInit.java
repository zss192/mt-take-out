package com.sky.bloom;

import com.sky.mapper.CategoryMapper;
import com.sky.utils.BloomFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BloomFilterInit {
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    @Autowired
    private CategoryMapper categoryMapper;

    @PostConstruct
    public void initializeBloomFilter() {
        // 查询数据库获取数据
        List<String> ids = categoryMapper.getId();

        // 将数据添加到布隆过滤器
        for (String id : ids) {
            bloomFilterUtil.put("dish_" + id);
        }
    }
}
