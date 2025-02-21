package com.davisellwood.website.common.components.markdown.rendering;

import java.util.Set;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlWriter;

public class CustomNodeRenderer implements NodeRenderer {
    public static class Factory implements HtmlNodeRendererFactory {
        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            return new CustomNodeRenderer(context);
        }
    }

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

        if (node.getNext() instanceof Link nextNode
                && node.getNext().getNext() instanceof Text nextNextNode
                && textNode.getLiteral().endsWith("[")
                && nextNextNode.getLiteral().startsWith("]")
        ) {
            html.text(textNode.getLiteral().substring(0, textNode.getLiteral().length() - 1));
            html.tag("a href=" + nextNode.getDestination());
            html.text(nextNode.getTitle());
            html.tag("/a");
            html.text(nextNextNode.getLiteral().substring(1));
            node.getNext().unlink();
            node.getNext().unlink();
            return;
        }

        html.text(textNode.getLiteral());
    }
}