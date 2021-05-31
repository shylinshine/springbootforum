package com.luntan.wql.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;


    @Test
    public void testSensitiveFileter() {
        String text = "一起来#赌&博?，吃炸鸡，喝啤酒,@开；，票,嫖一一娼";

        String tes = sensitiveFilter.filter(text);
        System.out.println(tes);

    }

}