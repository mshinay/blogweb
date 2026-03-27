package com.boot.blogserver.task;

import com.blog.constant.RedisConstant;
import com.blog.entry.ArticleStats;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleViewCountStringSyncTaskTests {

    @Mock
    private ArticleStatsMapper articleStatsMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Spy
    @InjectMocks
    private ArticleViewCountStringSyncTask articleViewCountStringSyncTask;

    @Test
    void should_map_key_and_value_to_increment_stats() {
        Set<String> viewKeys = new LinkedHashSet<>(List.of(
                RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "15",
                RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "27"
        ));
        doReturn(viewKeys).when(articleViewCountStringSyncTask).scanViewKeys();
        when(stringRedisTemplate.execute(any(), anyList())).thenReturn(List.of("100", "3"));

        articleViewCountStringSyncTask.syncViewCountsToDatabase();

        ArgumentCaptor<List<ArticleStats>> captor = ArgumentCaptor.forClass(List.class);
        verify(articleStatsMapper).batchIncrementViewCount(captor.capture());
        verify(stringRedisTemplate).execute(any(), anyList());

        List<ArticleStats> statsList = captor.getValue();
        assertEquals(2, statsList.size());
        Map<Long, Long> incrementMap = statsList.stream()
                .collect(Collectors.toMap(ArticleStats::getArticleId, ArticleStats::getViewCount));
        assertEquals(100L, incrementMap.get(15L));
        assertEquals(3L, incrementMap.get(27L));
    }

    @Test
    void should_skip_blank_value_and_illegal_key() {
        Set<String> orderedKeys = new LinkedHashSet<>(List.of(
                RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "15",
                "blog:article:view:bad-id",
                RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "29"
        ));
        doReturn(orderedKeys).when(articleViewCountStringSyncTask).scanViewKeys();
        when(stringRedisTemplate.execute(any(), anyList())).thenAnswer(invocation -> {
            List<String> keys = invocation.getArgument(1);
            if (keys.size() == 3 && keys.get(0).endsWith("15") && keys.get(1).endsWith("bad-id") && keys.get(2).endsWith("29")) {
                return List.of("7", "8", "");
            }
            return List.of("7", "", "8");
        });

        articleViewCountStringSyncTask.syncViewCountsToDatabase();

        ArgumentCaptor<List<ArticleStats>> captor = ArgumentCaptor.forClass(List.class);
        verify(articleStatsMapper).batchIncrementViewCount(captor.capture());
        List<ArticleStats> statsList = captor.getValue();
        assertEquals(1, statsList.size());
        assertEquals(15L, statsList.get(0).getArticleId());
        assertEquals(7L, statsList.get(0).getViewCount());
    }

    @Test
    void should_not_update_database_when_all_values_are_skipped() {
        Set<String> viewKeys = new LinkedHashSet<>(List.of(
                RedisConstant.ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX + "15",
                "blog:article:view:bad-id"
        ));
        doReturn(viewKeys).when(articleViewCountStringSyncTask).scanViewKeys();
        when(stringRedisTemplate.execute(any(), anyList())).thenReturn(List.of("", ""));

        articleViewCountStringSyncTask.syncViewCountsToDatabase();

        verify(articleStatsMapper, never()).batchIncrementViewCount(anyList());
        verify(stringRedisTemplate).execute(any(), anyList());
    }
}
