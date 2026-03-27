package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ArticleViewCountStringSyncTask {

    private static final DefaultRedisScript<List> READ_AND_DELETE_SCRIPT = createReadAndDeleteScript();

    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 60000)
    public void syncViewCountsToDatabase() {
        log.info("定时任务执行，同步文章浏览量增量（String 多 key 对照方案）");
        Set<String> viewKeys = scanViewKeys();
        if (viewKeys == null || viewKeys.isEmpty()) {
            return;
        }

        List<String> keyList = new ArrayList<>(viewKeys);
        List<String> viewCounts = stringRedisTemplate.execute(READ_AND_DELETE_SCRIPT, keyList);
        if (viewCounts == null || viewCounts.isEmpty()) {
            return;
        }

        List<ArticleStats> articleStatsList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < keyList.size() && i < viewCounts.size(); i++) {
            String key = keyList.get(i);
            String viewCount = viewCounts.get(i);
            if (key == null || key.isBlank() || viewCount == null || viewCount.isBlank()) {
                continue;
            }

            Long articleId = parseArticleIdFromKey(key);
            if (articleId == null) {
                continue;
            }

            ArticleStats articleStats = new ArticleStats();
            articleStats.setArticleId(articleId);
            articleStats.setViewCount(Long.parseLong(viewCount));
            articleStats.setUpdatedTime(now);
            articleStatsList.add(articleStats);
        }

        if (articleStatsList.isEmpty()) {
            return;
        }
        articleStatsMapper.batchIncrementViewCount(articleStatsList);
    }

    public Set<String> scanViewKeys() {
        return stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                            .match(RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "*")
                            .count(100)
                            .build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return keys;
        });
    }

    private Long parseArticleIdFromKey(String key) {
        if (!key.startsWith(RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX)) {
            log.warn("String 浏览量同步跳过非法 key: {}", key);
            return null;
        }
        String articleIdStr = key.substring(RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX.length());
        try {
            return Long.parseLong(articleIdStr);
        } catch (NumberFormatException ex) {
            log.warn("String 浏览量同步解析 articleId 失败, key={}", key);
            return null;
        }
    }

    private static DefaultRedisScript<List> createReadAndDeleteScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/article_view_count_string_get_and_delete.lua"));
        script.setResultType(List.class);
        return script;
    }
}
