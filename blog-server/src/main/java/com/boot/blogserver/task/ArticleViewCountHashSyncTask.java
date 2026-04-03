package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.blog.utils.ArticleViewSyncLogUtil;
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
        ArticleViewSyncLogUtil.SyncMetrics metrics = ArticleViewSyncLogUtil.start(ArticleViewSyncLogUtil.SCHEME_HASH);
        ArticleViewSyncLogUtil.logTaskStart(log, metrics);
        String outcome = "success";
        try {
            long redisStartNanoTime = System.nanoTime();
            metrics.markRedisPrepared(1, redisStartNanoTime);
            ArticleViewSyncLogUtil.logRedisPrepared(log, metrics);

            long luaStartNanoTime = System.nanoTime();
            List<String> hashEntries = stringRedisTemplate.execute(
                    READ_ALL_AND_DELETE_SCRIPT,
                    Collections.singletonList(RedisConstant.ARTICLE_VIEW_COUNT_HASH_KEY)
            );
            int luaResultCount = hashEntries == null ? 0 : hashEntries.size();
            metrics.markLuaDone(luaResultCount, luaStartNanoTime);
            ArticleViewSyncLogUtil.logLuaDone(log, metrics);
            if (hashEntries == null || hashEntries.isEmpty()) {
                ArticleViewSyncLogUtil.logJavaDone(log, metrics);
                ArticleViewSyncLogUtil.logDbDone(log, metrics);
                outcome = "lua_empty";
                return;
            }

            List<ArticleStats> articleStatsList = new ArrayList<>();
            long javaStartNanoTime = System.nanoTime();
            LocalDateTime now = LocalDateTime.now();
            long totalViewCount = 0L;
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
                long parsedViewCount = parseViewCount(articleIdField, viewCountValue);

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
                    ArticleViewSyncLogUtil.SCHEME_HASH,
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

    private Long parseArticleId(String articleIdField) {
        try {
            return Long.parseLong(articleIdField);
        } catch (NumberFormatException ex) {
            log.warn("article_view_sync stage=java_parse_failed scheme={} field={}", ArticleViewSyncLogUtil.SCHEME_HASH, articleIdField);
            return null;
        }
    }

    private long parseViewCount(String articleIdField, String viewCountValue) {
        try {
            return Long.parseLong(viewCountValue);
        } catch (NumberFormatException ex) {
            log.error(
                    "article_view_sync stage=java_parse_failed scheme={} field={} rawViewCount={}",
                    ArticleViewSyncLogUtil.SCHEME_HASH,
                    articleIdField,
                    viewCountValue
            );
            throw ex;
        }
    }

    private static DefaultRedisScript<List> createReadAllAndDeleteScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/article_view_count_hash_get_all_and_delete.lua"));
        script.setResultType(List.class);
        return script;
    }
}
