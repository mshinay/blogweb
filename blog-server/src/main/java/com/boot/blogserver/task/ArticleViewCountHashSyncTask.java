package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ArticleViewCountHashSyncTask {

    private static final DefaultRedisScript<List> READ_ALL_AND_DELETE_SCRIPT = createReadAllAndDeleteScript();

    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 60000)
    public void syncViewCountsToDatabase() {
        log.info("定时任务执行，同步文章浏览量增量（Hash 聚合方案）");
        List<String> hashEntries = stringRedisTemplate.execute(
                READ_ALL_AND_DELETE_SCRIPT,
                Collections.singletonList(RedisConstant.ARTICLE_VIEW_COUNT_HASH_KEY)
        );
        if (hashEntries == null || hashEntries.isEmpty()) {
            return;
        }

        List<ArticleStats> articleStatsList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i + 1 < hashEntries.size(); i += 2) {
            String articleIdField = hashEntries.get(i);
            String viewCountValue = hashEntries.get(i + 1);
            if (articleIdField == null || articleIdField.isBlank() || viewCountValue == null || viewCountValue.isBlank()) {
                continue;
            }

            Long articleId = parseArticleId(articleIdField);
            if (articleId == null) {
                continue;
            }

            ArticleStats articleStats = new ArticleStats();
            articleStats.setArticleId(articleId);
            articleStats.setViewCount(Long.parseLong(viewCountValue));
            articleStats.setUpdatedTime(now);
            articleStatsList.add(articleStats);
        }

        if (articleStatsList.isEmpty()) {
            return;
        }
        articleStatsMapper.batchIncrementViewCount(articleStatsList);
    }

    private Long parseArticleId(String articleIdField) {
        try {
            return Long.parseLong(articleIdField);
        } catch (NumberFormatException ex) {
            log.warn("Hash 浏览量同步解析 articleId 失败, field={}", articleIdField);
            return null;
        }
    }

    private static DefaultRedisScript<List> createReadAllAndDeleteScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/article_view_count_hash_get_all_and_delete.lua"));
        script.setResultType(List.class);
        return script;
    }
}
