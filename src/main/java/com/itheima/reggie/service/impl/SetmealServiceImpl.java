package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐，同时新增套餐与菜品的关联关系
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐基本信息setmeal
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((mealDishs)->{
            mealDishs.setSetmealId(setmealDto.getId());
            return mealDishs;
        }).collect(Collectors.toList());
        // 保存套餐与菜品的关联关系setmeal_dish
        setmealDishService.saveBatch(setmealDishes);
    }


    /**
     * 删除套餐，同时删除套餐与菜品的关联关系
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // sql: select count(*) from setmeal where id in (1,2,3) and status = 1
        // 查询套餐状态，判断是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        // 如果不能删除，抛出一个业务异常
        if(count > 0){
            log.error("【删除套餐】，套餐已经有预约，不能删除");
            throw new CustomException("套餐已经有预约，不能删除");
        }

        // 如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        // 删除关系表中的数据
        // sql: delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }
}
