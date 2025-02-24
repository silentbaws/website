package com.davisellwood.website.api.blog;


import static com.davisellwood.website.common.WebsiteConstants.Blog.BLOG_POSTS_FOLDER;
import static com.davisellwood.website.common.WebsiteConstants.Blog.DB_COLLECTION_NAME;
import static com.davisellwood.website.common.WebsiteConstants.Blog.PATH_PREFIX;

import com.davisellwood.website.common.components.markdown.BlogMarkdownRenderer;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import com.google.protobuf.Timestamp;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import proto.davisellwood.website.models.APIKeyOuterClass.APIKey;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPost;

@Controller
@Slf4j
public class BlogApiController {
    private final ObjectStore objectStore;
    private final Database database;

    @Inject
    public BlogApiController(SpringStorageProvider storageProvider, BlogMarkdownRenderer markdownRenderer) {
        this.objectStore = storageProvider.objectStore();
        this.database = storageProvider.database();
    }

    // TODO: Include username in request and get apikey for the username
    @PostMapping(PATH_PREFIX + "/upload")
    public ResponseEntity<String> uploadBlog(
            @RequestBody String body,
            @RequestHeader("api_key") String apiKey,
            @RequestHeader("is_public") String isPublicString,
            @RequestHeader("blog_id") String blogId,
            @RequestHeader("blog_title") String blogTitle,
            @RequestHeader("description") String description) {
        List<APIKey> apiKeys = database.getAll(APIKey.class, "api_keys");
        if (apiKeys.isEmpty() || !BCrypt.checkpw(apiKey, apiKeys.get(0).getKey())) {
            throw new AuthenticationCredentialsNotFoundException("API Key was not valid");
        }

        try {
            boolean isPublic = Boolean.parseBoolean(isPublicString);
            Instant currentInstant = Instant.now();
            Timestamp currentTime = Timestamp.newBuilder().setSeconds(currentInstant.getEpochSecond())
                    .setNanos(currentInstant.getNano()).build();
            Timestamp publishDate = currentTime;

            BlogPost currentPost = database.get(BlogPost.class, DB_COLLECTION_NAME, "blogId", blogId);

            if (currentPost != null) {
                log.info("Updating blog with post id {}", blogId);

                if (currentPost.getIsPublic()) {
                    publishDate = currentPost.getPublishDate();
                }
            }

            if (objectStore.put(BLOG_POSTS_FOLDER + blogId, body.getBytes(StandardCharsets.UTF_16LE))) {
                BlogPost newOrUpdatedPost = BlogPost.newBuilder()
                        .setBlogId(blogId)
                        .setTitle(blogTitle)
                        .setIsPublic(isPublic)
                        .setPublishDate(publishDate)
                        .setLastEdited(currentTime)
                        .setContentFilePath(BLOG_POSTS_FOLDER + blogId)
                        .setDescription(description)
                        .build();

                if (currentPost != null) {
                    database.update(DB_COLLECTION_NAME, "blogId", blogId, newOrUpdatedPost);
                } else {
                    database.put(DB_COLLECTION_NAME, newOrUpdatedPost);
                }
                return ResponseEntity.ok().body("Successfully uploaded blog post: " + blogTitle);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception e) {
            log.error("Error trying to upload new blog", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(PATH_PREFIX + "/attachFile")
    public ResponseEntity<String> postFile(
            @RequestBody byte[] body,
            @RequestHeader("api_key") String apiKey,
            @RequestHeader("blog_id") String blogId,
            @RequestHeader("image_name") String imageName) {
        
        List<APIKey> apiKeys = database.getAll(APIKey.class, "api_keys");
        if (apiKeys.isEmpty() || !BCrypt.checkpw(apiKey, apiKeys.get(0).getKey())) {
            throw new AuthenticationCredentialsNotFoundException("API Key was not valid");
        }
        log.info("ATTACHING FILE");

        return objectStore.putPublic(BLOG_POSTS_FOLDER + blogId + "/" + imageName, body)
                ? ResponseEntity.ok().body("Successfully uploaded image " + imageName + " for blog " + blogId)
                : ResponseEntity.badRequest().build();
    }
}
