package com.luntan.wql.dao;

import com.luntan.wql.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MessageMapperTest {

    @Autowired
    MessageMapper messageMapper;


    @Test
    public void testMessgae () {

        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);

        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);

        for (Message message : list) {

            System.out.println(message);

        }
//
        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count=messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);


    }

}