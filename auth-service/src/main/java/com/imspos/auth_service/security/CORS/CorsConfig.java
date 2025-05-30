package com.imspos.auth_service.security.CORS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // Allow all origins - you should restrict this in production
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList( "*"));
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        // Allow credentials
        config.setAllowCredentials(true);
        // How long the response to the preflight request can be cached
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}