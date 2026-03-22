package com.boot.blogserver.mapper;

import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentUserHistoryQueryDTO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentMapperXmlTests {

    @Test
    void pageQueryAdminShouldFocusOnManagementFilters() throws Exception {
        CommentAdminListDTO dto = new CommentAdminListDTO();
        dto.setUserId(11L);
        dto.setArticleId(101L);
        dto.setParentId(1L);
        dto.setRootId(2L);
        dto.setReplyUserId(12L);
        dto.setStatus(1);

        String sql = normalizeSql(getPageQueryAdminBoundSql(dto).getSql());

        assertTrue(sql.contains("c.user_id = ?"));
        assertTrue(sql.contains("c.article_id = ?"));
        assertTrue(sql.contains("c.status = ?"));
        assertFalse(sql.contains("c.parent_id = ?"));
        assertFalse(sql.contains("c.root_id = ?"));
        assertFalse(sql.contains("c.reply_user_id = ?"));
    }

    @Test
    void pageQueryAdminShouldExpandKeywordAcrossManagementSearchDimensions() throws Exception {
        CommentAdminListDTO dto = new CommentAdminListDTO();
        dto.setKeyword("spring");

        String sql = normalizeSql(getPageQueryAdminBoundSql(dto).getSql());

        assertTrue(sql.contains("c.content like concat('%', ?, '%')"));
        assertTrue(sql.contains("from user u"));
        assertTrue(sql.contains("u.username like concat('%', ?, '%')"));
        assertTrue(sql.contains("u.nickname like concat('%', ?, '%')"));
        assertTrue(sql.contains("from user ru"));
        assertTrue(sql.contains("ru.username like concat('%', ?, '%')"));
        assertTrue(sql.contains("ru.nickname like concat('%', ?, '%')"));
        assertTrue(sql.contains("from article a"));
        assertTrue(sql.contains("a.title like concat('%', ?, '%')"));
        assertTrue(sql.contains("a.slug like concat('%', ?, '%')"));
    }

    @Test
    void pageQueryCurrentUserShouldBeBoundToCurrentUserWithoutPublicStatusFilter() throws Exception {
        CommentUserHistoryQueryDTO dto = new CommentUserHistoryQueryDTO();
        dto.setArticleId(101L);

        String sql = normalizeSql(getPageQueryCurrentUserBoundSql(dto, 11L).getSql());

        assertTrue(sql.contains("from comment c"));
        assertTrue(sql.contains("c.user_id = ?"));
        assertTrue(sql.contains("c.article_id = ?"));
        assertFalse(sql.contains("status = 1"));
        assertFalse(sql.contains("c.root_id = 0"));
    }

    private BoundSql getPageQueryAdminBoundSql(CommentAdminListDTO dto) throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMapper(CommentMapper.class);

        String resource = "mapper/CommentMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder xmlMapperBuilder =
                    new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            xmlMapperBuilder.parse();
        }

        MappedStatement mappedStatement =
                configuration.getMappedStatement("com.boot.blogserver.mapper.CommentMapper.pageQueryAdmin");
        return mappedStatement.getBoundSql(dto);
    }

    private BoundSql getPageQueryCurrentUserBoundSql(CommentUserHistoryQueryDTO dto, Long currentUserId) throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMapper(CommentMapper.class);

        String resource = "mapper/CommentMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder xmlMapperBuilder =
                    new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            xmlMapperBuilder.parse();
        }

        MappedStatement mappedStatement =
                configuration.getMappedStatement("com.boot.blogserver.mapper.CommentMapper.pageQueryCurrentUser");
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("query", dto);
        parameterMap.put("currentUserId", currentUserId);
        return mappedStatement.getBoundSql(parameterMap);
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
