package com.luntan.wql.dao;



import com.luntan.wql.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

//    增删改的方法返回值一般都是int，影响的行数
    @Insert({
            "insert into login_ticket (user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")  //自动生成主键
    int insertLoginTicket(LoginTicket loginTicket);



    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);


    //修改凭证状态
    @Update({
            "<script>" ,
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus (String ticket,int status);





}
