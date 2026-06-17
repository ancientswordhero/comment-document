package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.ApiResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        boolean needsAuth = path.equals("/api/auth/me")
                || path.startsWith("/api/bookshelf")
                || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(req.getMethod()))
                || (path.matches("/api/books/\\d+/reviews") && !"GET".equalsIgnoreCase(req.getMethod()))
                || (path.startsWith("/api/users") && !"GET".equalsIgnoreCase(req.getMethod()))
                || path.startsWith("/api/notifications")
                || path.startsWith("/api/notes")
                || path.matches("/api/books/\\d+/notes.*");
        boolean needsAdmin = path.startsWith("/api/admin/");

        if (!needsAuth && !needsAdmin) {
            chain.doFilter(request, response);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(res, 401, "请先登录");
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeError(res, 401, "登录已过期，请重新登录");
            return;
        }

        if (needsAdmin && !"ADMIN".equals(jwtUtil.getRole(token))) {
            writeError(res, 403, "无管理员权限");
            return;
        }

        req.setAttribute("userId", jwtUtil.getUserId(token));
        req.setAttribute("role", jwtUtil.getRole(token));
        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse res, int code, String message)
            throws IOException {
        res.setStatus(code);
        res.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> error = ApiResponse.error(code, message);
        res.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
