package com.findseat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Validates JWT tokens for endpoints annotated with @RequireAuth.
 * Reads the Bearer token from the Authorization header, puts the user id/role
 * into request attributes, and enforces the admin rule when required.
 * Responses match the old backend: 401 { message } / 403 { message }.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Always allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        if (requireAuth == null) {
            return true; // public endpoint
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "No token provided");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("userId", Long.valueOf(claims.get("id").toString()));
            request.setAttribute("userEmail", claims.get("email", String.class));
            request.setAttribute("userName", claims.get("name", String.class));
            request.setAttribute("userRole", claims.get("role", String.class));

            if (requireAuth.admin() && !"admin".equals(claims.get("role", String.class))) {
                writeError(response, 403, "Admin access required");
                return false;
            }
            return true;
        } catch (Exception ex) {
            writeError(response, 401, "Invalid or expired token");
            return false;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
