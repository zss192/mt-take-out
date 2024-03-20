package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 根据id查询套餐
        SetmealVO setmealVO = setmealMapper.getById(id);
        // 根据id查询套餐对应的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 套餐起售、停售
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 如果想起售但套餐内有停售菜品，则不可起售
        if (status.equals(StatusConstant.ENABLE)) {
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            for (SetmealDish setmealDish : setmealDishes) {
                Dish dish = dishMapper.getById(setmealDish.getDishId());
                if (dish != null && dish.getStatus().equals(StatusConstant.DISABLE)) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        setmealMapper.startOrStop(status, id);
    }

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 向套餐表插入一条数据
        setmealMapper.insert(setmeal);
        // 获取insert生成的主键值
        Long setmealId = setmeal.getId();
        // 插入菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDishe -> {
                setmealDishe.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 修改套餐基本信息
        setmealMapper.update(setmeal);
        // 删除原有的菜品
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        // 重新插入菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDishe -> {
                setmealDishe.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void deleteBacth(List<Long> ids) {
        // 如果套餐是在售状态，则不可删除
        for (Long id : ids) {
            SetmealVO setmealVO = setmealMapper.getById(id);
            if (setmealVO.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        // 删除套餐菜品表的信息
        for (Long id : ids) {
            setmealDishMapper.deleteBySetmealId(id);
        }
        // 删除套餐表的信息
        setmealMapper.deleteByIds(ids);
    }

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
