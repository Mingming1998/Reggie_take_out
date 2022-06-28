package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.itheima.reggie.controller.EmployeeController;
import com.itheima.reggie.filter.LoginCheckFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 * 公共字段自动填充时使用
 *
 * 要点：公共数据自动填充，需要在公共字段上添加@TableField(fill = FieldFill.INSERT)
 *      自定义元数据对象处理器MyMetaObjectHandler
 *      重写insertFill和updateFill方法
 *      进一步不能用HttpServletRequest，怎么办？
 *      使用BaseContext(ThreadLocal)以线程为单位来获取当前登录用户的id
 */

@Component  // 让spring管理该类
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /* 想获得empId，但是这里不能引入HttpServletRequest，所以需要使用ThreadLocal来获取当前线程的唯一标识
     HttpServletRequest -> ThreadLocal

     什么是ThreadLocal？(set/get)
        ThreadLocal是一个线程安全的类，它的作用是：
        为每个线程提供单独一份存储空间，具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外则不能访问

     属于同一个线程的是：
          LoginCheckFilter.doFilter
          EmployeeController.update
          MyMetaObjectHandler.updateFill
     因此在上面三个方法上面加入获取当前线程id的方法即可
     代码：long id = Thread.currentThread().getId();
          log.info("当前线程id：{}", id);
    */

    /**
     * 插入填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]....");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        // 调用BaseContext(ThreadLocal)的get方法，获取当前线程的empId
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]....");
        log.info(metaObject.toString());

        long id = Thread.currentThread().getId();
        log.info("当前线程id：{}", id);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
