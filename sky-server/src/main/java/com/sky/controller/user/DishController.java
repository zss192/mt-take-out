package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.BloomFilterUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BloomFilterUtil bloomFilterUtil;


    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        String key = "dish_" + categoryId;

        // 先检查布隆过滤器，如果不存在，直接返回空结果
        if (!bloomFilterUtil.mightContain(key)) {
            return Result.success(new ArrayList<>());
        }

        // 查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && !list.isEmpty()) {
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        // 如果redis中不存在，查询数据库，将查到的数据放到redis中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);

        // 将 categoryId 加入布隆过滤器
        bloomFilterUtil.put(key);

        return Result.success(list);
    }

}
