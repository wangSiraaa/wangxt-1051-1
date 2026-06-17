package com.river.sand.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class UserNameDecodeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userName = httpRequest.getHeader("X-User-Name");
        if (userName != null && !userName.isEmpty()) {
            try {
                userName = URLDecoder.decode(userName, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {
            }
            DecodedUserNameRequest wrappedRequest = new DecodedUserNameRequest(httpRequest, userName);
            chain.doFilter(wrappedRequest, response);
            return;
        }
        chain.doFilter(request, response);
    }

    public static class DecodedUserNameRequest extends HttpServletRequestWrapper {
        private final String decodedUserName;

        public DecodedUserNameRequest(HttpServletRequest request, String decodedUserName) {
            super(request);
            this.decodedUserName = decodedUserName;
        }

        @Override
        public String getHeader(String name) {
            if ("X-User-Name".equalsIgnoreCase(name)) {
                return decodedUserName;
            }
            return super.getHeader(name);
        }
    }
}
