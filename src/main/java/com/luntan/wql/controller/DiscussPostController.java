package com.luntan.wql.controller;


import com.luntan.wql.entity.Comment;
import com.luntan.wql.entity.DiscussPost;
import com.luntan.wql.entity.Page;
import com.luntan.wql.entity.User;
import com.luntan.wql.service.CommentService;
import com.luntan.wql.service.DiscussPostService;
import com.luntan.wql.service.LikeService;
import com.luntan.wql.service.UserService;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.CommunityUtil;
import com.luntan.wql.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    UserService userService;


    @Autowired
    CommentService commentService;


    @Autowired
    private LikeService likeService;



    @RequestMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content) {

        User user = hostHolder.getUser();
        if(user == null) {

            return CommunityUtil.getJSONString(403,"你还没有登录哦");

        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0,"发布成功");

    }

    @RequestMapping("/detail/{discussPostId}")
    public String getDiscussPost (@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);

        model.addAttribute("post",post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //点赞
        //数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);

        //点赞状态

        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);

        model.addAttribute("likeStatus",likeStatus);




        // 评论分页信息     page.getOffset()从哪一行开始，0,5,15
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());



        //评论；给帖子的评论
        //回复：给评论的评论
        //评论的列表


        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());


        //  !important 构造一个map，对要展示的对象统一的封装
        //评论VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            //评论vo
            for (Comment comment : commentList) {
                System.out.println(comment);

                Map<String,Object> commentVo =new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));





                //点赞
                //数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);


                //点赞状态

                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);


                model.addAttribute("likeStatus",likeStatus);







                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                //回复的vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();

                if (replyList !=null) {
                    for (Comment reply : replyList) {

                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));

                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());

                        replyVo.put("target",target);

                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);

                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0: likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);


                        replyVoList.add(replyVo);




                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());


                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);


            }

        }
        model.addAttribute("comments",commentVoList);


        return "site/discuss-detail";




    }



}
