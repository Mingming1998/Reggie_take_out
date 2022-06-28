package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j  // log
@SpringBootApplication
/**
 * @SpringBootApplication
 * @SpringBootConfiguration 配置
 * @EnableAutoConfiguration 自动配置
 * @ComponentScan           组件扫描
 */
@ServletComponentScan  // LoginCheckFilter(扫描WebFilter注解)
@EnableTransactionManagement // 开启事务管理
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("ReggieApplication start success...");
    }
}
