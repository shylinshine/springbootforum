package com.luntan.wql.service;

import com.luntan.wql.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class DiscussPostServiceTest {
    @Autowired
    DiscussPostService discussPostService;

    @Test
    public void testDis() {


        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("woowowowow");
        discussPost.setCreateTime(new Date());
        discussPost.setContent("sdasd");
        discussPost.setUserId(103);
        discussPost.setCommentCount(1);
        discussPost.setScore(11);
        discussPost.setStatus(1);
        discussPost.setType(0);
        discussPost.setId(288);



        discussPostService.addDiscussPost(discussPost);

    }

}