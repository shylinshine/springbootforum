package com.luntan.wql.config;


import com.luntan.wql.interceptor.InterceptorTest;
import com.luntan.wql.interceptor.LoginRequiredInterceptor;
import com.luntan.wql.interceptor.LoginTicketInterceptor;
import com.luntan.wql.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//不加注解无效
@Configuration
public class WebMvcConfig  implements WebMvcConfigurer  {

    @Autowired
    private InterceptorTest interceptorTest;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //添加一个拦截器
        registry.addInterceptor(interceptorTest)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpg","/**/*.jpeg") //不用拦截的
                .addPathPatterns("/register","/login");


        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpg","/**/*.jpeg"); //不用拦截的


        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpg","/**/*.jpeg"); //不用拦截的


        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpg","/**/*.jpeg"); //不用拦截的


    }
}
