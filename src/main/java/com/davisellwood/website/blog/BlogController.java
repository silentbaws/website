package com.davisellwood.website.blog;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Document;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.Parser;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import lombok.extern.slf4j.Slf4j;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPost;
import proto.davisellwood.website.models.BlogPostOuterClass.BlogPostMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class BlogController {
    public static final String PATH_PREFIX = "blog";

    private final ObjectStore objectStore;
    private final Database database;
    
    @Inject
    public BlogController(SpringStorageProvider storageProvider) {
        this.objectStore = storageProvider.objectStore();
        this.database = storageProvider.database();
    }

    @PostMapping(PATH_PREFIX + "/new")
    public ResponseEntity<String> postNewBlogImage(
        Model model,
        @RequestBody String body,
        @RequestHeader("api_key") String apiKey,
        @RequestHeader("is_public") String isPublicString,
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

            boolean isPublic = Boolean.parseBoolean(isPublicString);
            Instant currentInstant = Instant.now();
            Timestamp currentTime = Timestamp.newBuilder().setSeconds(currentInstant.getEpochSecond()).setNanos(currentInstant.getNano()).build();
            Timestamp publishDate = currentTime;

            if (blogMap.containsPosts(blogId)) {
                log.info("Updating blog with post id {}", blogId);
                
                if (updatedBlogs.get(blogId).getIsPublic()) {
                    publishDate = updatedBlogs.get(blogId).getPublishDate();
                }
            }

            if (objectStore.put("blog-posts/" + blogId, body.getBytes(StandardCharsets.UTF_16LE))) {
                BlogPost newOrUpdatedPost = BlogPost.newBuilder().setBlogId(blogId).setTitle(blogTitle).setIsPublic(isPublic).setPublishDate(publishDate).setLastEdited(currentTime).setContentFilePath("blog-posts/" + blogId).build();
    
                database.put("blog-posts", DBEntry.newBuilder().setByteValue(BlogPostMap.newBuilder().putAllPosts(updatedBlogs).putPosts(blogId, newOrUpdatedPost).build().toByteString()).build());
                return ResponseEntity.ok().body("Successfully uploaded blog post: " + blogTitle);
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch  (Exception e) {
            log.error("Error trying to upload new blog", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(PATH_PREFIX + "/attachFile")
    public ResponseEntity<String> postFile(
        Model model,
        @RequestBody byte[] body,
        @RequestHeader("api_key") String apiKey,
        @RequestHeader("blog_id") String blogId,
        @RequestHeader("image_name") String imageName
    ) {
        log.info("ATTACHING FILE");

        if (!BCrypt.checkpw(apiKey, database.get("blog-post-api-key").getStringValue())) {
            throw new AuthenticationCredentialsNotFoundException("API Key was not valid");
        }

        return objectStore.putPublic("blog-posts/"+blogId+"/"+imageName, body) ? ResponseEntity.ok().body("Successfully uploaded image " + imageName + " for blog " + blogId) : ResponseEntity.badRequest().build();
    }
    
    @GetMapping(PATH_PREFIX + "/view/{blog_id}")
    public String getNewBlog(Model model, @PathVariable("blog_id") String blogId) {
        BlogPostMap blogPosts;
        try {
            blogPosts = BlogPostMap.parseFrom(database.get("blog-posts").getByteValue());
        } catch (InvalidProtocolBufferException e) {
            return "error";
        }

        BlogPost post = blogPosts.getPostsMap().get(blogId);

        // TODO: Inject the markdown generator and provide some methods
        List<Extension> extensions = List.of(TablesExtension.create(), HeadingAnchorExtension.create(), ImageAttributesExtension.create(), StrikethroughExtension.create(), FootnotesExtension.create());

        Parser parser = Parser.builder().linkProcessor(new CustomLinkProcessor(blogId)).extensions(extensions).build();
        Node document = parser.parse(new String(objectStore.get(post.getContentFilePath()).get(), StandardCharsets.UTF_16LE));
        HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(new CustomImageAttributeProvider.CustomImageAttributeProviderFactory()).nodeRendererFactory(new CustomNodeRenderer.Factory()).extensions(extensions).build();

        model.addAttribute("publishDate", ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.getPublishDate().getSeconds()), ZoneId.of("America/Toronto")).format(DateTimeFormatter.ofPattern("dd MMM YYYY h:mm a")));
        model.addAttribute("lastUpdatedDate", ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.getLastEdited().getSeconds()), ZoneId.of("America/Toronto")).format(DateTimeFormatter.ofPattern("dd MMM YYYY h:mm a")));
        model.addAttribute("title", post.getTitle());
        model.addAttribute("content", renderer.render(document));

        return "blog/post";
    }

    public static class CustomNodeRenderer implements NodeRenderer {

        private final HtmlWriter html;

        CustomNodeRenderer(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(Text.class);
        }

        @Override
        public void render(Node node) {
            Text textNode = (Text) node;

            if (node.getNext() instanceof Link && node.getNext().getNext() instanceof Text) {
                Link nextNode = (Link) node.getNext();
                Text nextNextNode = (Text) node.getNext().getNext();
                if (textNode.getLiteral().endsWith("[") && nextNextNode.getLiteral().startsWith("]")) {
                    html.text(textNode.getLiteral().substring(0, textNode.getLiteral().length() - 1));
                    html.tag("a href=" + nextNode.getDestination());
                    html.text(nextNode.getTitle());
                    html.tag("/a");
                    html.text(nextNextNode.getLiteral().substring(1));
                    node.getNext().unlink();
                    node.getNext().unlink();
                    return;
                }
            }

            html.text(textNode.getLiteral());
        }
        
        public static class Factory implements HtmlNodeRendererFactory {
            @Override
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new CustomNodeRenderer(context);
            }
            
        }
    }

    public static class CustomImageAttributeProvider implements AttributeProvider {
        public static class CustomImageAttributeProviderFactory implements AttributeProviderFactory {

            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new CustomImageAttributeProvider();
            }
            
        }

        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (tagName.equals("table")) {
                attributes.put("class", "table table-striped table-light table-hover table-bordered");
                return;
            } else if (tagName.equals("th")) {
                attributes.put("scope", "col");
                if (attributes.get("align") != null) {
                    attributes.put("style", "text-align:"+attributes.get("align")+";");
                }
                return;
            } else if (tagName.equals("img")) {
                attributes.put("width", "100%");
                return;
            }

            if (node.getParent() instanceof Document && !Set.of("h1", "h2", "h3", "h4", "h5", "h6").contains(tagName)) {
                boolean isFootnote = node instanceof FootnoteDefinition;

                if (!isFootnote) {
                    attributes.put("class", "lead");
                }
            } else if (Set.of("td", "th").contains(tagName)) {
                attributes.put("class", "lead");
            }
        }
    }

    public static class CustomLinkProcessor implements LinkProcessor {
        private final String blogId;

        public CustomLinkProcessor(String blogId) {
            this.blogId = blogId;
        }

        @Override
        public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {
            if (linkInfo.marker() != null && "!".equals(linkInfo.marker().getLiteral()) && linkInfo.text().startsWith("[") && linkInfo.text().endsWith("]")) {
                // TODO: Rework this and store image locations in blog entry in db
                return LinkResult.replaceWith(new Image("https://davisellwood-website.nyc3.cdn.digitaloceanspaces.com/blog-posts/"+blogId+"/"+linkInfo.text().substring(1, linkInfo.text().length() - 1).toString(), "title"), scanner.position()).includeMarker();
            }
            if (linkInfo.marker() == null && linkInfo.text().startsWith("#")) {
                String headingText = linkInfo.text().substring(1);
                String headingLink = linkInfo.text().replace(" ", "-").toLowerCase();
                return LinkResult.replaceWith(new Link(headingLink, headingText), scanner.position());
            }
            return LinkResult.none();
        }
    }
}
