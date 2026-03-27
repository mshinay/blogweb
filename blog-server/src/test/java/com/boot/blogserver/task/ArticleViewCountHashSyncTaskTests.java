package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleViewCountHashSyncTaskTests {

    @Mock
    private ArticleStatsMapper articleStatsMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private ArticleViewCountHashSyncTask articleViewCountHashSyncTask;

    @Test
    void should_map_hash_entries_to_increment_stats() {
        when(stringRedisTemplate.execute(any(), anyList()))
                .thenReturn(List.of("15", "7", "27", "3"));

        articleViewCountHashSyncTask.syncViewCountsToDatabase();

        ArgumentCaptor<List<ArticleStats>> captor = ArgumentCaptor.forClass(List.class);
        verify(articleStatsMapper).batchIncrementViewCount(captor.capture());
        verify(stringRedisTemplate).execute(any(), anyList());

        List<ArticleStats> statsList = captor.getValue();
        assertEquals(2, statsList.size());
        assertEquals(15L, statsList.get(0).getArticleId());
        assertEquals(7L, statsList.get(0).getViewCount());
        assertEquals(27L, statsList.get(1).getArticleId());
        assertEquals(3L, statsList.get(1).getViewCount());
    }

    @Test
    void should_skip_illegal_field_and_blank_value() {
        when(stringRedisTemplate.execute(any(), anyList()))
                .thenReturn(List.of("bad-id", "7", "29", ""));

        articleViewCountHashSyncTask.syncViewCountsToDatabase();

        verify(articleStatsMapper, never()).batchIncrementViewCount(anyList());
        verify(stringRedisTemplate).execute(any(), anyList());
    }

    @Test
    void should_not_update_database_when_hash_is_empty() {
        when(stringRedisTemplate.execute(any(), anyList()))
                .thenReturn(List.of());

        articleViewCountHashSyncTask.syncViewCountsToDatabase();

        verify(articleStatsMapper, never()).batchIncrementViewCount(anyList());
        verify(stringRedisTemplate).execute(any(), anyList());
    }

    @Test
    void should_use_hash_counter_key_for_sync() {
        when(stringRedisTemplate.execute(any(), anyList()))
                .thenReturn(List.of("15", "1"));

        articleViewCountHashSyncTask.syncViewCountsToDatabase();

        ArgumentCaptor<List<String>> keyCaptor = ArgumentCaptor.forClass(List.class);
        verify(stringRedisTemplate).execute(any(), keyCaptor.capture());
        assertEquals(List.of(RedisConstant.ARTICLE_VIEW_COUNT_HASH_KEY), keyCaptor.getValue());
    }
}
