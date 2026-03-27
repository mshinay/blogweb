package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ArticleViewCountSyncTask {
    @Autowired
    ArticleStatsMapper articleStatsMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 60000)
    public void syncViewCountsToDatabase() {
        log.info("定时任务执行，同步文章浏览量增量");
        Set<String> viewKeys = scanViewKeys();
        if(viewKeys==null||viewKeys.isEmpty()){return;}
        List<String> keyList = new ArrayList<>(viewKeys);
        List<String> viewCounts = stringRedisTemplate.opsForValue().multiGet(keyList);
        List<ArticleStats> articleStatsList = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++)  {
            if(keyList.get(i)==null|| keyList.get(i).isBlank()){continue;}
            String articleIdStr = keyList.get(i).substring(RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX.length());
            Long id = Long.parseLong(articleIdStr);

            if(viewCounts.get(i)==null||viewCounts.get(i).isBlank()){continue;}
            ArticleStats articleStats = new ArticleStats();
            articleStats.setArticleId(id);
            articleStats.setViewCount(Long.parseLong(viewCounts.get(i)));
            articleStats.setUpdatedTime(LocalDateTime.now());
            articleStatsList.add(articleStats);
        }
        if (articleStatsList.isEmpty()) {
            return;
        }
        articleStatsMapper.batchIncrementViewCount(articleStatsList);
        stringRedisTemplate.delete(viewKeys);
    }

    public Set<String> scanViewKeys() {
        return stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();

            Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions()
                            .match(RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "*")
                            .count(100)
                            .build()
            );

            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }

            return keys;
        });
    }


}
