package com.luntan.wql.controller;



import com.luntan.wql.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.activation.CommandMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class TestController {

    @RequestMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        Cookie cookie =new Cookie("code", CommunityUtil.generateUUID());
        //指定哪些路径有效
        cookie.setPath(("/community/alpha"));
        //设置cookie存在时间，会存到硬盘
        cookie.setMaxAge(60*10);
        //发送cookie
        response.addCookie(cookie);

        return  "set_cookie";

    }

    @RequestMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get-cookie";
    }

    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name,int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");

    }



}
