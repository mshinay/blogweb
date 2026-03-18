package com.boot.blogserver.mapper;



import com.blog.entry.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username} ")
    User getByUsername(String username);

    @Select("select * from user where id = #{id} ")
    User getById(Long id);

    @Select("select * from user where email = #{email} ")
    User getByEmail(String email);

    @Select("select username from user where id = #{id} ")
    String getNameById(Long id);

    @Insert("insert into user(username,password,email,nickname,avatar_url,bio,role,status,last_login_time,created_time,updated_time) " +
            "values(#{username},#{password},#{email},#{nickname},#{avatarUrl},#{bio},COALESCE(#{role}, 1),COALESCE(#{status}, 1),#{lastLoginTime},#{createdTime},COALESCE(#{updatedTime}, #{createdTime}))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);

    void update(User user);

    List<User> getUsersByIds(@Param("userIds") Set<Long> userIds);
}
