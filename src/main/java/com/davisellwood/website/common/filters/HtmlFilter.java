package com.davisellwood.website.common.filters;

import in.wilsonl.minifyhtml.Configuration;
import in.wilsonl.minifyhtml.MinifyHtml;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(10)
public class HtmlFilter implements Filter {
    protected FilterConfig config;
    private static final Configuration minifyConfig = new Configuration.Builder()
            .setKeepHtmlAndHeadOpeningTags(true)
            .setKeepSpacesBetweenAttributes(true)
            .setDoNotMinifyDoctype(true)
            .setEnsureSpecCompliantUnquotedAttributeValues(true)
            .setMinifyCss(true)
            .setMinifyJs(true)
            .build();


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper((HttpServletResponse) response);
        
        chain.doFilter(request, capturingResponseWrapper);

        if (response.getContentType() != null && (response.getContentType().contains("text/html")
                || response.getContentType().contains("text/javascript"))) {

            String content = capturingResponseWrapper.getCaptureAsString();

            // Hacky way to compress javascript before sending over network.
            // TODO: replace with actual minifying library for javascript
            if (response.getContentType().contains("text/javascript")) {
                content = "<script>" + content + "</script>";
            }

            String replacedContent = MinifyHtml.minify(content, minifyConfig);

            if (response.getContentType().contains("text/javascript")) {
                replacedContent = replacedContent.substring("<script>".length(), replacedContent.length() - "</script>".length());
            }

            response.setContentLength(replacedContent.getBytes(capturingResponseWrapper.getCharacterEncoding()).length);
            response.getWriter().write(replacedContent);
        } else {
            response.getOutputStream().write(capturingResponseWrapper.getCaptureAsBytes());
        }
        
    }
}