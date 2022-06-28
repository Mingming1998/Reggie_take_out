package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成了登录
 * 防止不登录就可以访问内部页面
 */
@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符***
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到的请求:{}",requestURI);

        // 用户访问这些路径直接放行
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",  // 移动端发送短信
                "/user/login"  // 移动端登录
        };
        // 2、判断本次请求是否需要处理(该次访问是否处于登录状态)
        boolean check = check(urls, requestURI);

        // 3、如果不需要处理，直接放行
        if(check){
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);  // 放行
            return;
        }

        // 4-1、判断登录状态(session中含有employee的登录信息)，如果已经登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户{}已经登录", request.getSession().getAttribute("employee"));  // id

            // 调用BaseContext(ThreadLocal)的set方法，将employee.empId存入到ThreadLocal中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 4-2、判断登录状态(session中含有employee的登录信息)，如果已经登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户{}已经登录", request.getSession().getAttribute("user"));  // id

            // 调用BaseContext(ThreadLocal)的set方法，将employee.empId存入到ThreadLocal中
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录，跳转到登录页面");
        // 5、如果未登录，则返回未登录的结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
