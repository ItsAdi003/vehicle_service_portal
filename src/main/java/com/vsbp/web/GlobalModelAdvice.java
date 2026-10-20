package com.vsbp.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page needs (shown in the footer so the deployed environment is always visible). */
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
}
