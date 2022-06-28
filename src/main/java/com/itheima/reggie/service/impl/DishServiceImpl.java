package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService{

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     */
    @Override
    @Transactional  // 因为要操作多张表，因此需要加上事务注解，并且在启动类上加上@EnableTransactionManagement注解，开启事务管理功能
    public void saveWithFlavor(DishDto dishDto) {
        // 1. 新增菜品dish表
        this.save(dishDto);

        Long dishId = dishDto.getId();  // 获取新增菜品的id
        List<DishFlavor> flavors = dishDto.getFlavors();  // 获取菜品对应的口味数据
//        for (DishFlavor flavor : flavors) {
//            flavor.setDishId(dishId);
//            dishFlavorService.save(flavor);
//        }
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        // 2. 新增菜品对应的口味数据dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据菜品id查询菜品及其对应的口味数据
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 1. 查询菜品dish表
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 2. 查询菜品对应的口味数据dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        // 3. 封装成DishDto对象
//        BeanUtils.copyProperties(dish, dishDto);
//        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 根据更新id更新菜品及其对应的口味数据
     */
    @Override
    public void updateWithFlavor(DishDto dishDto){
        // 1. 更新菜品dish表
        this.updateById(dishDto);
        // 2. 删除菜品对应的口味数据dish_flavor表delete
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        // 3. 新增菜品对应的口味数据dish_flavor表insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
