package com.blog.utils;

public class ArticleUtil {

    static public String generateSummary(String content) {
        if (content.length() <= 20) {
            return content;
        }else {
            String summary = content.substring(0, 20);
            return summary+"....";
        }
    }
}
