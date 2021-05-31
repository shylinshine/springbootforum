package com.luntan.wql.controller;


import com.luntan.wql.entity.Event;
import com.luntan.wql.entity.User;
import com.luntan.wql.event.EventProducer;
import com.luntan.wql.service.LikeService;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.CommunityUtil;
import com.luntan.wql.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    @PostMapping("/like")
    @ResponseBody
    //entityType这些数据是由js传递过来的
    public String like(int entityType, int entityId,int entityUserId,int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId,entityUserId);

        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);


        //触发点赞事件
        if(likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);


            eventProducer.fireEvent(event);

        }



        return CommunityUtil.getJSONString(0,null,map);

    }




}
