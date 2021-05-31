package com.luntan.wql.controller;


import com.luntan.wql.entity.Comment;
import com.luntan.wql.entity.DiscussPost;
import com.luntan.wql.entity.Event;
import com.luntan.wql.event.EventProducer;
import com.luntan.wql.service.CommentService;
import com.luntan.wql.service.DiscussPostService;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.HostHolder;
import jdk.nashorn.internal.runtime.regexp.joni.ast.EncloseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

        //没做判断，后面会做统一处理
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);



        //触发评论事件

        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setData("postId",discussPostId)
                .setEntityId(comment.getEntityId());


        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if(comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);



        return "redirect:/discuss/detail/" + discussPostId;




    }
}
