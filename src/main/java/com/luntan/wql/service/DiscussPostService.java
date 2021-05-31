package com.luntan.wql.service;



import com.luntan.wql.dao.CommentMapper;
import com.luntan.wql.dao.DiscussPostMapper;
import com.luntan.wql.entity.Comment;
import com.luntan.wql.entity.DiscussPost;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService  implements CommunityConstant {

    @Autowired
    private DiscussPostMapper discussPostMapper;

   @Autowired
   private SensitiveFilter sensitiveFilter;


    @Autowired
    private DiscussPostService discussPostService;


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }


    public int addDiscussPost(DiscussPost post) {
        if(post == null) {
            throw new IllegalArgumentException("参数不能为空");

        }

        //转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));


        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id) {

        return discussPostMapper.selectDiscussPostById(id);
    }

    //更新帖子评论数
    public int updateCommentCount (int id,int commentCount) {

        return discussPostMapper.updateCommentCount(id, commentCount);


    }



}
