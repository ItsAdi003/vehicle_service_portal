package com.vsbp.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/** Protects /slots, /bookings and /admin: login required, and ADMIN role for /admin. */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/admin") && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return false;
        }
        return true;
    }
}
