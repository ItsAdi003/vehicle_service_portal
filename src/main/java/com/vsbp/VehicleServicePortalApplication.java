package com.vsbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extending SpringBootServletInitializer lets the same artefact run
 * both as {@code java -jar vehicle-service-portal.war} and inside an external Tomcat 10.1.
 */
@SpringBootApplication
public class VehicleServicePortalApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(VehicleServicePortalApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(VehicleServicePortalApplication.class, args);
    }
}
