package com.davisellwood.website.common.components.markdown.parsing;

import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;

public class CustomLinkProcessor implements LinkProcessor {
    private final String blogId;

    public CustomLinkProcessor(String blogId) {
        this.blogId = blogId;
    }

    @Override
    public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {
        if (linkInfo.marker() != null
                && "!".equals(linkInfo.marker().getLiteral())
                && linkInfo.text().startsWith("[")
                && linkInfo.text().endsWith("]")) {
            String strippedLink = linkInfo.text().substring(1, linkInfo.text().length() - 1);

            return LinkResult.replaceWith(
                    new Image(String.join("/", blogId, "files", strippedLink), ""),
                    scanner.position()).includeMarker();
        } else if (linkInfo.marker() == null && linkInfo.text().startsWith("#")) {
            String headingText = linkInfo.text().substring(1);
            String headingLink = linkInfo.text().replace(" ", "-").toLowerCase();
            return LinkResult.replaceWith(new Link(headingLink, headingText), scanner.position());
        }

        return LinkResult.none();
    }
}
