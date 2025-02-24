package com.davisellwood.website.common.components.markdown;

import com.davisellwood.website.common.components.markdown.parsing.CustomLinkProcessor;
import com.davisellwood.website.common.components.markdown.rendering.CustomAttributeProvider;
import com.davisellwood.website.common.components.markdown.rendering.CustomNodeRenderer;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

@Component
public class BlogMarkdownRenderer {
    private static final List<Extension> EXTENSIONS = List.of(
            TablesExtension.create(),
            HeadingAnchorExtension.create(),
            ImageAttributesExtension.create(),
            StrikethroughExtension.create(),
            FootnotesExtension.create());

    private final HtmlRenderer htmlRenderer;

    public BlogMarkdownRenderer() {
        htmlRenderer = HtmlRenderer.builder()
                .attributeProviderFactory(new CustomAttributeProvider.Factory())
                .nodeRendererFactory(new CustomNodeRenderer.Factory())
                .extensions(EXTENSIONS)
                .build();
    }

    // TODO: Caching?
    public String renderMarkdown(String blogId, String markdown) {
        Parser markdownParser = Parser.builder()
                .extensions(EXTENSIONS)
                .linkProcessor(new CustomLinkProcessor(blogId))
                .build();
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }
}
