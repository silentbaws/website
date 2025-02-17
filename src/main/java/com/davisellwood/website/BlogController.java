package com.davisellwood.website;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;

import lombok.extern.slf4j.Slf4j;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPost;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPostMap;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPostMapOrBuilder;


@Slf4j
@Controller
public class BlogController {
    private static final String BLOG_KEY_PREFIX = "blog-post-";

    public static final String PATH_PREFIX = "blog";

    private final ObjectStore objectStore;
    private final Database database;
    
    @Inject
    public BlogController(SpringStorageProvider storageProvider) {
        this.objectStore = storageProvider.objectStore();
        this.database = storageProvider.database();
    }

    @RequestMapping(PATH_PREFIX + "/new")
    public ResponseEntity<String> postNewBlog(
        Model model,
        @RequestBody String body,
        @RequestHeader("api_key") String apiKey,
        @RequestHeader("is_public") String isPublic,
        @RequestHeader("blog_id") String blogId,
        @RequestHeader("blog_title") String blogTitle
    ) {
        if (!BCrypt.checkpw(apiKey, database.get("blog-post-api-key").getStringValue())) {
            throw new AuthenticationCredentialsNotFoundException("API Key was not valid");
        }

        DBEntry blogs = database.get("blog-posts");
        if (blogs == null) {
            blogs = DBEntry.newBuilder().setByteValue(BlogPostMap.newBuilder().build().toByteString()).build();
            database.put("blog-posts", blogs);
        }

        try {
            BlogPostMap blogMap = BlogPostMap.parseFrom(blogs.getByteValue());
            Map<String, BlogPost> updatedBlogs = blogMap.getPostsMap();

            if (blogMap.containsPosts(blogId)) {
                log.info("Updating blog with post id {}", blogId);
            }
            // TODO: update with actual path and upload content to spaces
            BlogPost newOrUpdatedPost = BlogPost.newBuilder().setBlogId(blogId).setTitle(blogTitle).setIsPublic(false).setContentFilePath("").build();

            database.put("blog-posts", DBEntry.newBuilder().setByteValue(BlogPostMap.newBuilder().putAllPosts(updatedBlogs).putPosts(blogId, newOrUpdatedPost).build().toByteString()).build());
            return ResponseEntity.ok().build();
        } catch  (Exception e) {
            log.error("Error trying to upload new blog", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
