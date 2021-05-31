package com.luntan.wql.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class InterceptorTest implements HandlerInterceptor {


    //实例化日志组件
    private static  final Logger logger = LoggerFactory.getLogger(InterceptorTest.class);


    //在controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        logger.debug("preHander: "+ handler.toString());
        System.out.println("slldal");

        return true;
    }



    //在调用完controller之后执行,模版之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        logger.debug("postHander: " + handler.toString());
        System.out.println("kkk");
    }
//    在templateEngine之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {


        logger.debug("after :" + handler.toString());

    }
}
