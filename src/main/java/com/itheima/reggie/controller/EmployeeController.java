package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * 1、将页面提交的密码password进行 MD5 加密处理
     * 2、根据页面提交的用户名username查询数据库
     * 3、如果没有查询到数据，则返回登录失败的结果
     * 4、进行密码比对，如果不一致，则返回登录失败的结果
     * 5、查看员工状态，如果为 已禁用状态，则返回被禁用的结果信息
     * 6、登录成功，将员工id 存入Session并返回登录成功的结果 (request -- session)
     */
    // R:将服务端数据传递给前端显示
    // Employee employee 是json数据，因此需要加@RequestBody
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        // 1、将页面提交的密码password进行 MD5 加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);// Unique唯一索引

        // 3、如果没有查询到数据，则返回登录失败的结果
        if(emp == null){
            return R.error("用户名不存在");
        }

        // 4、进行密码比对，如果不一致，则返回登录失败的结果
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }

        // 5、查看员工状态，如果为 已禁用状态，则返回被禁用的结果信息
        if(emp.getStatus() == 0){
            return R.error("账号被禁用");
        }

        // 6、登录成功，将员工id 存入Session并返回登录成功的结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 员工登出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1、清除session
        request.getSession().removeAttribute("employee");
        // 2、返回登出成功的结果
        return R.success("登出成功");
    }

    /**
     * 新增员工
     */
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        // 将页面提交的密码password进行 MD5 加密处理（初始密码123456）
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // [insert]调用MyMetaObjectHandler.insertFill()进行公共数据类型填充
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        // 获得当前登录用户id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询员工，当前页：{}，每页记录数：{}，查询条件：{}", page, pageSize, name);
        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件name
        // isNotEmpty弃用，使用hasText/isNotBlank
        queryWrapper.like(StringUtils.hasText(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        // Service中写好了page方法，直接调用即可
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id更新员工信息
     */
    @PutMapping  // get获得，post新增，put修改，delete删除
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("修改员工，员工信息：{}", employee.toString());

        long id = Thread.currentThread().getId();
        log.info("当前线程id：{}", id);

        // 获得当前登录用户id
        // 雪花算法导致精度丢失
        // json对long型数据进行处理时丢失精度，导致提交的id和数据库中的id不一致
        // 遗失精度
        // 解决方案：将long型转成String字符串
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改员工成功");
    }

    /**
     * 根据id查询员工信息
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工，id：{}", id);
        Employee employee = employeeService.getById(id);
        if(employee == null){
            return R.error("查询不到该员工");
        }
        return R.success(employee);
    }
}
