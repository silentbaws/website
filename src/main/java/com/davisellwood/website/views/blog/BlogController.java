package com.davisellwood.website.views.blog;

import static com.davisellwood.website.common.WebsiteConstants.Blog.BLOG_POSTS_FOLDER_URL;
import static com.davisellwood.website.common.WebsiteConstants.Blog.DB_COLLECTION_NAME;
import static com.davisellwood.website.common.WebsiteConstants.Blog.PATH_PREFIX;
import static com.davisellwood.website.common.WebsiteConstants.ERROR_404_PAGE;

import com.davisellwood.website.common.components.markdown.BlogMarkdownRenderer;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPost;

@Slf4j
@Controller
public class BlogController {
    private final ObjectStore objectStore;
    private final Database database;
    private final BlogMarkdownRenderer markdownRenderer;

    @Inject
    public BlogController(SpringStorageProvider storageProvider, BlogMarkdownRenderer markdownRenderer) {
        this.objectStore = storageProvider.objectStore();
        this.database = storageProvider.database();
        this.markdownRenderer = markdownRenderer;
    }

    @GetMapping(PATH_PREFIX)
    public String getHome(Model model) {
        List<BlogPost> blogPosts = database.getAll(BlogPost.class, DB_COLLECTION_NAME)
                .stream()
                .filter(post -> post.getIsPublic())
                .sorted((leftPost, rightPost) -> {
                    return Long.compare(
                        leftPost.getPublishDate().getSeconds(),
                        rightPost.getPublishDate().getSeconds());
                }).toList();

        List<Map<String, String>> attributes = blogPosts.stream().map(
                post -> Map.of(
                    "title", post.getTitle(),
                    "publishDate", convertEpochSecondsToTimestamp(post.getPublishDate().getSeconds()),
                    "id", post.getBlogId()
                )).toList();

        model.addAttribute("posts", attributes);

        return PATH_PREFIX + "/home";
    }
    

    @GetMapping(PATH_PREFIX + "/view/{blog_id}")
    public String getBlogPost(Model model, @PathVariable("blog_id") String blogId) {
        BlogPost post = database.get(BlogPost.class, DB_COLLECTION_NAME, "blogId", blogId);

        if (post == null) {
            return ERROR_404_PAGE;
        }

        Optional<byte[]> markdownContentBytes = objectStore.get(post.getContentFilePath());

        if (markdownContentBytes.isEmpty()) {
            return ERROR_404_PAGE;
        }

        String markdownContent = new String(markdownContentBytes.get(), StandardCharsets.UTF_8);

        model.addAttribute("publishDate", convertEpochSecondsToTimestamp(post.getPublishDate().getSeconds()));
        model.addAttribute("lastUpdatedDate", convertEpochSecondsToTimestamp(post.getLastEdited().getSeconds()));
        model.addAttribute("title", post.getTitle());
        model.addAttribute("content", markdownRenderer.renderMarkdown(blogId, markdownContent));

        return PATH_PREFIX + "/post";
    }
    
    @GetMapping(PATH_PREFIX + "/view/{blog_id}/files/{file_id}")
    public RedirectView getBlogFile(
            @PathVariable("blog_id") String blogId,
            @PathVariable("file_id") String fileId) {
        return new RedirectView(String.join("/", BLOG_POSTS_FOLDER_URL, blogId, fileId));
    }

    private String convertEpochSecondsToTimestamp(long epochSeconds) {
        return ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(epochSeconds),
            ZoneId.of("America/Toronto")
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a"));
    }
}
