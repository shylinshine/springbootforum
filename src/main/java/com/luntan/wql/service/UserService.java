package com.luntan.wql.service;



import com.luntan.wql.dao.LoginTicketMapper;
import com.luntan.wql.dao.UserMapper;
import com.luntan.wql.entity.LoginTicket;
import com.luntan.wql.entity.User;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.CommunityUtil;
import com.luntan.wql.util.MailClient;
import com.luntan.wql.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;


    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;


    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id) {
        //return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null) {
            user = initCache(id);
        }

        return user;
    }

//注册
    public Map<String,Object> register(User user){


        Map<String, Object> map = new HashMap<>();
        //对空值判断
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");

        }
        //判断账号
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空");
            return  map;

        }

        //判断密码
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空");
            return  map;

        }
        //判断邮箱是否为空
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空");
            return  map;

        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该账号已经存在");
            return map;
        }

        //验证邮箱
        User ue = userMapper.selectByName(user.getEmail());
        if(ue != null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() +user.getSalt()));//md5+随机盐
        //默认没有激活
        user.setType(0);
        user.setStatus(0);
        //激活码随机生成的
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置随机头像,路径是牛客网的
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));

        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //发送激活邮件
        Context context =new Context();
        context.setVariable("email",user.getEmail());

        //http://localhost:8080/comunity/activation/101/code
        //拼接url
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println(url+"已发送");

        context.setVariable("url",url);

        String content =templateEngine.process("mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);




        return  map;

    }

    //激活的方法
    public int activation(int userId, String code) {
        User user =userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return  ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)) {

            userMapper.updateStatus(userId,1);
            //清理缓存
            clearCache(userId);
            return  ACTIVATION_SUCCESS;

        }else {
            return ACTIVATION_FAILURE;
        }

    }


    //登录
    //password不能和库里的密码直接去比
    public Map<String,Object> login(String username,String password,int expiredSecondes){
        Map<String,Object> map =new HashMap<>();


        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;

        }

        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;

        }

        //验证账号
        User user =userMapper.selectByName(username);
        if(user ==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }


        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");


            return map;
        }
        //验证密码
         password = CommunityUtil.md5(password + user.getSalt());//的得到加密之后的密码

        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码不正确");
        }

        //程序执行到这里，登录成功，生成登录凭证
        //构造对象
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() +expiredSecondes * 1000));



//        loginTicketMapper.insertLoginTicket(loginTicket);
        //存到redis
        String redisKey = RedisKeyUtil.getTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());



        return map;

    }


    //登出
    public void logout(String ticket) {

        //loginTicketMapper.updateStatus(ticket,1);

        String redisKey = RedisKeyUtil.getTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);


    }


    public LoginTicket findLoginTicket(String ticket) {

        String redisKey = RedisKeyUtil.getTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //     return loginTicketMapper.selectByTicket(ticket);

    }

    public int updateHeader (int userId, String headUrl) {


       // return userMapper.updateHeader(userId, headUrl);
        int rows = userMapper.updateHeader(userId, headUrl);
        //clear
        clearCache(userId);
        return rows;

    }



    public User findUserByName(String name) {

        return userMapper.selectByName(name);
    }

    //1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);

    }


    //2.取不到时，初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
