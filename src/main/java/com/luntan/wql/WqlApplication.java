package com.luntan.wql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableAspectJAutoProxy
public class WqlApplication {

    @PostConstruct
    public void init() {
        //解决netty启动冲突的问题
        //see netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors","false");


    }

    public static void main(String[] args) {
        SpringApplication.run(WqlApplication.class, args);
    }

}
