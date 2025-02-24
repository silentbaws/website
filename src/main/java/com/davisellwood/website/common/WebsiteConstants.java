package com.davisellwood.website.common;

public class WebsiteConstants {
    private WebsiteConstants() {}

    public static final String ERROR_404_PAGE = "error";

    public static class Blog {
        private Blog() {}

        public static final String DB_COLLECTION_NAME = "blog_posts";
        public static final String PATH_PREFIX = "blog";
        public static final String BLOG_POSTS_FOLDER = "blog-posts/";
        public static final String BLOG_POSTS_FOLDER_URL = "https://davisellwood-website.nyc3.cdn.digitaloceanspaces.com/blog-posts";
    }
}
