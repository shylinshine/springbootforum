package com.luntan.wql.controller;

import com.google.code.kaptcha.Producer;

import com.luntan.wql.entity.User;
import com.luntan.wql.service.UserService;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.CommunityUtil;
import com.luntan.wql.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @Autowired
    private UserService userService;



    @Autowired
    private Producer kaptchaProducer;


    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path")
    private String contextPath;


    @RequestMapping("/register")
    public String getRegisterPage() {


        return "site/register";
    }


    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);


        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，激活邮件已发送，请尽快激活");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));


            return "site/register";
        }

    }


    //登录

    @GetMapping("/login")
    public String getLoginPage() {

            return "site/login";

    }



    @RequestMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，你的账号可以正常使用了！");
            //跳到激活
            model.addAttribute("target", "/login");

        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经被激活了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败");
            model.addAttribute("target", "/index");
        }


        return "site/operate-result";
    }


    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        //返回给客户端
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);

        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);



        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberme, Model model,/*,HttpSession session,*/HttpServletResponse response,@CookieValue("kaptchaOwner") String kaptchaOwner) {

        //检查验证码
        //从session中取出验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");

        //重构后。。
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {

            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {

            model.addAttribute("codeMsg","验证码不正确");


            return  "site/login";


        }

        //检查账号，密码
        int expiredSecondes = rememberme ? REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SCONDES;
        Map<String, Object> map = userService.login(username, password, expiredSecondes);

        // 检查凭证
        if (map.containsKey("ticket")) {

            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            //设置有效路径
            cookie.setPath(contextPath);
            //设置有效时间
            cookie.setMaxAge(expiredSecondes);

            response.addCookie(cookie);

            return "redirect:/index";

        }else  {

            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));


            return "site/login";

        }


    }
    //登出
    @RequestMapping("/logout")
    public String logout (@CookieValue("ticket") String ticket) {
        userService.logout(ticket);

        return "redirect:/login";


    }

}