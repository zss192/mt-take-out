package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据ids查询套餐
     * @param ids
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);
}
