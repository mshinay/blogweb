package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.blog.utils.ArticleViewSyncLogUtil;
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

    //@Scheduled(fixedRate = 60000)
    public void syncViewCountsToDatabase() {
        ArticleViewSyncLogUtil.SyncMetrics metrics = ArticleViewSyncLogUtil.start(ArticleViewSyncLogUtil.SCHEME_STRING);
        ArticleViewSyncLogUtil.logTaskStart(log, metrics);
        String outcome = "success";
        try {
            long redisStartNanoTime = System.nanoTime();
            Set<String> viewKeys = scanViewKeys();
            int redisCandidateCount = viewKeys == null ? 0 : viewKeys.size();
            metrics.markRedisPrepared(redisCandidateCount, redisStartNanoTime);
            ArticleViewSyncLogUtil.logRedisPrepared(log, metrics);

            if (viewKeys == null || viewKeys.isEmpty()) {
                ArticleViewSyncLogUtil.logLuaDone(log, metrics);
                ArticleViewSyncLogUtil.logJavaDone(log, metrics);
                ArticleViewSyncLogUtil.logDbDone(log, metrics);
                outcome = "no_redis_candidate";
                return;
            }

            List<String> keyList = new ArrayList<>(viewKeys);
            long luaStartNanoTime = System.nanoTime();
            List<String> viewCounts = stringRedisTemplate.execute(READ_AND_DELETE_SCRIPT, keyList);
            int luaResultCount = viewCounts == null ? 0 : viewCounts.size();
            metrics.markLuaDone(luaResultCount, luaStartNanoTime);
            ArticleViewSyncLogUtil.logLuaDone(log, metrics);
            if (viewCounts == null || viewCounts.isEmpty()) {
                ArticleViewSyncLogUtil.logJavaDone(log, metrics);
                ArticleViewSyncLogUtil.logDbDone(log, metrics);
                outcome = "lua_empty";
                return;
            }

            List<ArticleStats> articleStatsList = new ArrayList<>();
            long javaStartNanoTime = System.nanoTime();
            LocalDateTime now = LocalDateTime.now();
            long totalViewCount = 0L;
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
                long parsedViewCount = parseViewCount(key, viewCount);

                ArticleStats articleStats = new ArticleStats();
                articleStats.setArticleId(articleId);
                articleStats.setViewCount(parsedViewCount);
                articleStats.setUpdatedTime(now);
                articleStatsList.add(articleStats);
                totalViewCount += parsedViewCount;
            }
            metrics.markJavaDone(articleStatsList.size(), totalViewCount, javaStartNanoTime);
            ArticleViewSyncLogUtil.logJavaDone(log, metrics);

            if (articleStatsList.isEmpty()) {
                ArticleViewSyncLogUtil.logDbDone(log, metrics);
                outcome = "java_empty";
                return;
            }
            long dbStartNanoTime = System.nanoTime();
            //articleStatsMapper.batchIncrementViewCount(articleStatsList);
            metrics.markDbDone(dbStartNanoTime);
            ArticleViewSyncLogUtil.logDbDone(log, metrics);
        } catch (RuntimeException ex) {
            outcome = "failed";
            log.error(
                    "article_view_sync stage=failed scheme={} errorType={} errorMessage={} totalElapsedMs={}",
                    ArticleViewSyncLogUtil.SCHEME_STRING,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    metrics.getTotalElapsedMs(),
                    ex
            );
            throw ex;
        } finally {
            ArticleViewSyncLogUtil.logFinish(log, metrics, outcome);
        }
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
            log.warn("article_view_sync stage=java_parse_failed scheme={} key={}", ArticleViewSyncLogUtil.SCHEME_STRING, key);
            return null;
        }
    }

    private long parseViewCount(String key, String viewCount) {
        try {
            return Long.parseLong(viewCount);
        } catch (NumberFormatException ex) {
            log.error(
                    "article_view_sync stage=java_parse_failed scheme={} key={} rawViewCount={}",
                    ArticleViewSyncLogUtil.SCHEME_STRING,
                    key,
                    viewCount
            );
            throw ex;
        }
    }

    private static DefaultRedisScript<List> createReadAndDeleteScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/article_view_count_string_get_and_delete.lua"));
        script.setResultType(List.class);
        return script;
    }
}
