package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类，保存和获取当前用户登录的empId
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 设置当前线程的empId
    public static void setCurrentId(Long id){  // 工具类中的方法应该设置成static
        threadLocal.set(id);
    }

    // 获取当前线程的empId
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
