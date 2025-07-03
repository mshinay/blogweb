package com.boot.blogserver.mapper;



import com.blog.entry.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
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

    @Insert("insert into user(username,password,email,role,avatar_url,create_time) " +
            "values(#{username},#{password},#{email},#{role},#{avatarUrl},#{createTime}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);

    void update(User user);

    List<User> getUsersByIds(Set<Long> userIds);
}
