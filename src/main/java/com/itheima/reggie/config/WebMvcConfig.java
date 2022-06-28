package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration  // 说明是配置类
public class WebMvcConfig extends WebMvcConfigurationSupport {
    // 设置静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开启静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    // 扩展SpringMvc的消息转换器
    // id: long -> String
    // {"code":1,"msg":null,"data":{"id":"1515620377059975170","username":"wuda",
    // "name":"大朗","password":"e10adc3949ba59abbe56e057f20f883e","phone":"13412345678",
    // "sex":"1","idNumber":"987456321123456987","status":0,"createTime":"2022-04-17 17:17:11",
    // "updateTime":"2022-06-24 13:09:22","createUser":"1","updateUser":"1"},"map":{}}
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("自定义消息转换器 被调用!");
        // 创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将消息转换器添加到converters中
        converters.add(0, messageConverter);  // 0:放在最前面优先使用
    }
}
