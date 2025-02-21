package com.davisellwood.website.views.blog;

import static com.davisellwood.website.common.WebsiteConstants.Blog.BLOG_POSTS_DB_KEY;
import static com.davisellwood.website.common.WebsiteConstants.Blog.BLOG_POSTS_FOLDER_URL;
import static com.davisellwood.website.common.WebsiteConstants.Blog.PATH_PREFIX;

import com.davisellwood.website.common.components.markdown.BlogMarkdownRenderer;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPost;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPostMap;



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

    @GetMapping(PATH_PREFIX + "/view/{blog_id}/files/{file_id}")
    public RedirectView getMethodName(
            @PathVariable("blog_id") String blogId,
            @PathVariable("file_id") String fileId) {
        return new RedirectView(String.join("/", BLOG_POSTS_FOLDER_URL, blogId, fileId));
    }
    

    @GetMapping(PATH_PREFIX + "/view/{blog_id}")
    public String getNewBlog(Model model, @PathVariable("blog_id") String blogId) {
        BlogPostMap blogPosts;
        try {
            blogPosts = BlogPostMap.parseFrom(database.get(BLOG_POSTS_DB_KEY).getByteValue());
        } catch (InvalidProtocolBufferException e) {
            return "error";
        }

        BlogPost post = blogPosts.getPostsMap().get(blogId);
        Optional<byte[]> markdownContentBytes = objectStore.get(post.getContentFilePath());

        if (markdownContentBytes.isEmpty()) {
            return "error";
        }

        String markdownContent = new String(markdownContentBytes.get(), StandardCharsets.UTF_16LE);

        model.addAttribute("publishDate", 
                ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(post.getPublishDate().getSeconds()),
                    ZoneId.of("America/Toronto")
                ).format(DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a")));
        
        model.addAttribute("lastUpdatedDate", 
                ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(post.getLastEdited().getSeconds()),
                    ZoneId.of("America/Toronto")
                ).format(DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a")));
        
        model.addAttribute("title", post.getTitle());
        model.addAttribute("content", markdownRenderer.renderMarkdown(blogId, markdownContent));

        return PATH_PREFIX + "/post";
    }
}
