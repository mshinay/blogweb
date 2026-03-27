package com.blog.constant;

public class RedisConstant {
   public static final String ARTICLE_DETAIL_KEY_PREFIX = "blog:article:detail:";
   public static final String COMMENT_PREVIEW_KEY_PREFIX = "blog:comment:preview:";
   public static final String ARTICLE_VIEW_COUNT_STRING_KEY_PREFIX = "blog:article:view:";
   public static final String ARTICLE_VIEW_COUNT_HASH_KEY = "blog:article:view:hash";
   public static final Long ARTICLE_DETAIL_TTL = 30L;
}
