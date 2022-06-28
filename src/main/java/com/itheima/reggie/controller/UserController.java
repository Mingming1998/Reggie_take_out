package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            // 调用阿里云服务api发送短信
            // SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            // 生成的验证码保存到session中
//            session.setAttribute(phone, code);

            // 将生成的验证码缓存到Redis中，并设置有效期为五分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.success("手机验证码验证成功");
        }

        return R.error("手机验证码验证失败");
    }

    /**
     * 移动端用户登录
     */
    @PostMapping("/login")
    // phone code 两个参数 用Map接受就可以了
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();

        // 从Session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);

        // 从Redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        // 进行验证码的比对
        if(codeInSession != null && codeInSession.equals(code)){
            // 如果验证码一致，则登录成功

            // 判断手机号是否在用户表中，如果不在，就自动注册用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                // 注册用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);  // 注册成功save
            }
            // 登录成功，将用户信息保存到session中
            session.setAttribute("user", user.getId());

            // 登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }
}
