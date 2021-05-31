package com.luntan.wql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

//通用的Bean，交给springboot管理
@Component
public class MailClient {

    private static final Logger logger= LoggerFactory.getLogger(MailClient.class);


    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private  String from;


    public  void sendMail(String to, String subject,String content) {
        try {
            MimeMessage message =mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message);
//            构建
            helper.setFrom(from);//设置发件人
            helper.setTo(to);//设置收件人
            helper.setSubject(subject);
            helper.setText(content,true);//设置文本
            //发送
            mailSender.send(helper.getMimeMessage());


        } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
        }


    }



}
