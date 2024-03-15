package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealDishService {
    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    SetmealVO getById(Long id);
}
