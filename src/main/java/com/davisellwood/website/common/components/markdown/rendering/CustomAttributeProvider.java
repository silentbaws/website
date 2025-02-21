package com.davisellwood.website.common.components.markdown.rendering;

import java.util.Map;
import java.util.Set;
import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;

public class CustomAttributeProvider implements AttributeProvider {
    public static class Factory implements AttributeProviderFactory {
        @Override
        public AttributeProvider create(AttributeProviderContext context) {
            return new CustomAttributeProvider();
        }
    }

    private static final Set<String> HEADING_ELEMENTS = Set.of("h1", "h2", "h3", "h4", "h5", "h6");

    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        boolean isTopLevelNode = node.getParent() instanceof Document;
        boolean isFootnote = node instanceof FootnoteDefinition;
        boolean isHeading = HEADING_ELEMENTS.contains(tagName);
        boolean isTableRow = "tr".equals(tagName);
        boolean isTable = "table".equals(tagName);


        switch (tagName) {
            case "table":
                attributes.put("class", "table table-striped table-light table-hover table-bordered");
                break;

            case "th":
                attributes.put("scope", "col");
                String alignment = attributes.get("align");
                if (alignment != null) {
                    attributes.put("style", "text-align:" + alignment + ";");
                }
                break;

            case "img":
                attributes.put("width", "100%");
                break;

            default:
                break;
        }

        // Headings and tables with the lead class render incorrectly
        // Footnotes look better smaller than the rest of the body text
        // Only apply to top level nodes as it **should** propagate down
        if (!isHeading && !isTable && !isFootnote && (isTopLevelNode || isTableRow)) {
            attributes.put("class", "lead");
        }
    }
}