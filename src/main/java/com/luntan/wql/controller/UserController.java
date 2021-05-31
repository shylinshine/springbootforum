package com.luntan.wql.controller;


import com.luntan.wql.annotation.LoginRequired;
import com.luntan.wql.entity.User;
import com.luntan.wql.service.FollowService;
import com.luntan.wql.service.LikeService;
import com.luntan.wql.service.UserService;
import com.luntan.wql.util.CommunityConstant;
import com.luntan.wql.util.CommunityUtil;
import com.luntan.wql.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static  final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Value("${community.path.upload}")
    private String uploadPath;



    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @LoginRequired
    @RequestMapping("/setting")
    public String getSettngPage() {

        return "site/setting";
    }


    @LoginRequired
    @RequestMapping("/upload")
    public String uploadHeader(MultipartFile headerImage , Model model) {

        if(headerImage == null ) {
            model.addAttribute("error","你还没选择图片");

            return "site/setting";

        }

        String originalFilename = headerImage.getOriginalFilename();//原始名字
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        if (StringUtils.isBlank(suffix)) {

            model.addAttribute("error","你还没选择照片");
            return "site/sitting";
        }

        //生成随机文件名
        String fileName  = CommunityUtil.generateUUID() +suffix;

        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" +e.getMessage());
            throw  new RuntimeException("上传文件失败，服务器发生异常");

        }

        //更新当前用户头像的路径（web访问路径）
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath +"/user/header/" +fileName;


        userService.updateHeader(user.getId(),headUrl);

        return "redirect:/index";



    }



    //访问头像

    @RequestMapping("/header/{fileName}")
    public void getHeadr(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        //写在（）里会关闭，相当于finally
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
                ) {

            //缓冲区
            byte[] buffer =new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {

                os.write(buffer,0,b);
            }




        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }

    }
    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        //用户的基本信息
        model.addAttribute("user",user);


        //点赞的数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);

        model.addAttribute("followeeCount",followeeCount);


        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        //是否已经关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);

        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "site/profile";
    }


}
