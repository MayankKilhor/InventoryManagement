package com.imspos.auth_service.security;


import com.imspos.auth_service.model.ApiAccessControl;
import com.imspos.auth_service.model.Authority;
import com.imspos.auth_service.security.Handler.AuthEntryPointJwt;
import com.imspos.auth_service.security.Handler.CustomAccessDeniedHandler;
import com.imspos.auth_service.security.JWT.JwtFilter;
import com.imspos.auth_service.service.ApiAccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CorsFilter corsFilter;
    private final JwtFilter jwtFilter;
    private final CustomHeaderFilter customHeaderFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;
    private final ApiAccessControlService apiAccessControlService;

    @Autowired
    public SecurityConfig(
            CorsFilter corsFilter,
            JwtFilter jwtFilter,
            CustomHeaderFilter customHeaderFilter,
            AuthEntryPointJwt unauthorizedHandler,
            CustomAccessDeniedHandler accessDeniedHandler,
            @Lazy UserDetailsService userDetailsService,
            ApiAccessControlService apiAccessControlService
    ) {
        this.corsFilter = corsFilter;
        this.jwtFilter = jwtFilter;
        this.customHeaderFilter = customHeaderFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.userDetailsService = userDetailsService;
        this.apiAccessControlService = apiAccessControlService;
    }


    private static final String[] AUTH_WHITELIST = {

            "/auth/signUp",
            "/auth/login",
            "/auth/authorities/create-god-user",
            "/auh/hello"
    };

    private static final String[] ROLE_WHITELIST = {
            "/api/authorities/createUserRole",
            "/api/authorities/createRole"
    };


    private static final String ADD_ROLE_AUTHORITY = "ADD_USER_ROLE";


    




@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // First configure all the static paths
    http.cors(Customizer.withDefaults())
            .addFilterBefore(corsFilter, ChannelProcessingFilter.class)
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(unauthorizedHandler)
                    .accessDeniedHandler(accessDeniedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(customHeaderFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(AUTH_WHITELIST).permitAll();
                auth.requestMatchers(ROLE_WHITELIST).hasAuthority(ADD_ROLE_AUTHORITY);
                auth.requestMatchers("/auth/authorities/defaultConfiguration").permitAll();
                auth.requestMatchers("/**").permitAll();
                auth.requestMatchers(HttpMethod.PATCH, "/**").denyAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").denyAll()
                        .requestMatchers(HttpMethod.TRACE, "/**").denyAll()
                        .requestMatchers(HttpMethod.HEAD, "/**").denyAll()
                        .requestMatchers(HttpMethod.PUT, "/**").denyAll()
                        .requestMatchers(HttpMethod.DELETE, "/**").denyAll();
            });

    // Then add dynamic authorization rules
    try {
        List<ApiAccessControl> apiAccessList = apiAccessControlService.getAccessControlList();
        Map<String, Set<String>> endpointAuthorityMap = new HashMap<>();
        List<String> publicEndpoints = new ArrayList<>();

        for (ApiAccessControl apiAccess : apiAccessList) {
            if (Boolean.TRUE.equals(apiAccess.getPublic())) {
                publicEndpoints.add(apiAccess.getEndpointPath());
            } else if (apiAccess.getAuthorities() != null && !apiAccess.getAuthorities().isEmpty()) {
                Set<String> authorityNames = apiAccess.getAuthorities().stream()
                        .map(Authority::getName)
                        .collect(Collectors.toSet());
                endpointAuthorityMap.put(apiAccess.getEndpointPath(), authorityNames);
            }
        }

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(publicEndpoints.toArray(new String[0])).permitAll();

            for (Map.Entry<String, Set<String>> entry : endpointAuthorityMap.entrySet()) {
                String endpoint = entry.getKey();
                String[] authorityArray = entry.getValue().toArray(new String[0]);
                auth.requestMatchers(endpoint).hasAnyAuthority(authorityArray);
            }

            auth.anyRequest().authenticated();
        });
    } catch (IllegalStateException e) {
        // Fallback if access control list isn't initialized yet
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    }

    http.authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();

    }
}
