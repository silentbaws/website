package com.davisellwood.website.common;

public class WebsiteConstants {
    private WebsiteConstants() {}

    public static class Blog {
        private Blog() {}

        public static final String PATH_PREFIX = "blog";
        public static final String BLOG_POSTS_DB_KEY = "blog-posts";
        public static final String BLOG_POSTS_FOLDER = "blog-posts/";
        public static final String BLOG_POSTS_FOLDER_URL = "https://davisellwood-website.nyc3.cdn.digitaloceanspaces.com/blog-posts";
        public static final String BLOG_POSTS_API_KEY = "blog-post-api-key";
    }
}
