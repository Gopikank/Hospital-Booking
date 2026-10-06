package com.hospital.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Allow static resources & public endpoints
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
            path.startsWith("/error/") || path.equals("/") || path.equals("/index.jsp") ||
            path.startsWith("/auth/") || path.startsWith("/api/available-tokens") ||
            path.startsWith("/api/queue-status") || path.startsWith("/api/doctors") ||
            path.equals("/patient/live-queue.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        String role = loggedIn ? (String) session.getAttribute("role") : null;

        // Check if request is AJAX
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        // If not logged in and accessing protected section
        if (!loggedIn) {
            if (path.startsWith("/patient/") || path.startsWith("/doctor/") || path.startsWith("/admin/") ||
                path.startsWith("/api/")) {
                if (isAjax) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"success\":false,\"message\":\"Session expired or unauthorized. Please login.\"}");
                } else {
                    res.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=session_expired");
                }
                return;
            }
        } else {
            // Role-based Access Control
            if (path.startsWith("/patient/") && !"PATIENT".equals(role)) {
                sendForbidden(req, res, isAjax);
                return;
            }
            if (path.startsWith("/doctor/") && !"DOCTOR".equals(role)) {
                sendForbidden(req, res, isAjax);
                return;
            }
            if (path.startsWith("/admin/") && !"ADMIN".equals(role)) {
                sendForbidden(req, res, isAjax);
                return;
            }
        }

        // Prevent browser caching of sensitive pages
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        chain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletRequest req, HttpServletResponse res, boolean isAjax) throws IOException {
        if (isAjax) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\":false,\"message\":\"Access forbidden: insufficient role permissions.\"}");
        } else {
            res.sendRedirect(req.getContextPath() + "/error/403.jsp");
        }
    }

    @Override
    public void destroy() {}
}
