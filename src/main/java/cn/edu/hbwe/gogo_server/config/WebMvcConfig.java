package cn.edu.hbwe.gogo_server.config;

import cn.edu.hbwe.gogo_server.utils.UserLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new UserLoginInterceptor())
//                .addPathPatterns("/edu/**") // 拦截地址
//                .excludePathPatterns("/user/login");// 开放登录路径
    }

}