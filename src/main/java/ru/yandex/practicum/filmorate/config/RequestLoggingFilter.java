package ru.yandex.practicum.filmorate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request,
                            ContentCachingResponseWrapper response,
                            long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        if (status >= 400) {
            log.warn("{} {} - {} ({} ms)", method, uri, status, duration);
        } else {
            log.debug("{} {} - {} ({} ms)", method, uri, status, duration);
        }

        if (log.isTraceEnabled() && !isSensitiveEndpoint(uri)) {
            String requestBody = getContentAsString(request.getContentAsByteArray());
            if (!requestBody.isEmpty()) {
                log.trace("Request body: {}", requestBody);
            }
        }
    }

    private boolean isSensitiveEndpoint(String uri) {
        return uri.contains("/login") ||
                uri.contains("/password") ||
                uri.contains("/auth");
    }

    private String getContentAsString(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
                path.startsWith("/h2-console");
    }
}
