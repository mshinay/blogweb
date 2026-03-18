package com.boot.blogserver.mapper;

import com.blog.entry.Tag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface TagMapper {

    @Insert("insert into tag(name,slug,status,created_time,updated_time) " +
            "values(#{name},#{slug},COALESCE(#{status}, 1),#{createdTime},#{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Tag tag);

    @Select("select * from tag where id = #{id}")
    Tag getById(Long id);

    @Select("select * from tag where slug = #{slug}")
    Tag getBySlug(String slug);

    @Select("select * from tag where status = 1 order by id asc")
    List<Tag> listEnabled();

    List<Tag> getByIds(@Param("tagIds") Set<Long> tagIds);

    int update(Tag tag);
}
