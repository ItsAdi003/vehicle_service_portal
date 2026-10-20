package com.vsbp.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page needs (shown in the header/footer so user and environment are always visible). */
@ControllerAdvice
public class GlobalModelAdvice {

    private final String environment;
    private final String version;

    public GlobalModelAdvice(@Value("${app.environment:local}") String environment,
                             @Value("${app.version:dev}") String version) {
        this.environment = environment;
        this.version = version;
    }

    @ModelAttribute("appEnvironment")
    public String appEnvironment() {
        return environment;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        return version;
    }

    /** The logged-in user, or null for guests. */
    @ModelAttribute("me")
    public SessionUser me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);
    }
}
