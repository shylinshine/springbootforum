package com.luntan.wql.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)            //作用范围
@Retention(RetentionPolicy.RUNTIME)   //作用周期
public @interface LoginRequired {


}
