package com.boot.blogserver.mapper;

import com.blog.entry.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface CategoryMapper {

    @Insert("insert into category(name,slug,sort,status,created_time,updated_time) " +
            "values(#{name},#{slug},COALESCE(#{sort}, 0),COALESCE(#{status}, 1),#{createdTime},#{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Category category);

    @Select("select * from category where id = #{id}")
    Category getById(Long id);

    @Select("select * from category where slug = #{slug}")
    Category getBySlug(String slug);

    @Select("select * from category where status = 1 order by sort asc, id asc")
    List<Category> listEnabled();

    List<Category> getByIds(@Param("categoryIds") Set<Long> categoryIds);

    int update(Category category);
}
