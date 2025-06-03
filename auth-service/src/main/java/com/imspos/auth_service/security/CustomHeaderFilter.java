package com.imspos.auth_service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imspos.auth_service.payload.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;

@Component
@Order(1) // Set the order to control filter execution order
public class CustomHeaderFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomHeaderFilter.class);
    private static final String MACHINE_ID_HEADER = "x-machineId";
    private static final String CUSTOM_MACHINE_ID = "123456"; // Set your custom machine ID here

    private static final String[] EXCLUDED_PATHS = {
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/swagger-resources/",
            "/webjars/",
            "/configuration/ui",
            "/configuration/security",
            "/favicon.ico",
            "/error",
            "/actuator/health",
            "/api/auth/public" // Add any other public endpoints
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        try {
            // Check if the request URI should be excluded from header validation
            if (shouldExcludeFromHeaderValidation(requestURI)) {
                logger.debug("Excluding path from header validation: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            String existingMachineId = request.getHeader(MACHINE_ID_HEADER);

            if (existingMachineId == null) {
                logger.warn("Missing x-machineId header for path: {}", requestURI);
                sendErrorResponse(response, "Missing x-machineId header", HttpStatus.BAD_REQUEST);
                return;
            }

            if (!existingMachineId.equals(CUSTOM_MACHINE_ID)) {
                logger.warn("Invalid x-machineId header value: {} for path: {}", existingMachineId, requestURI);
                sendErrorResponse(response, "Invalid x-machineId header value", HttpStatus.UNAUTHORIZED);
                return;
            }

            logger.debug("Valid x-machineId header found for path: {}", requestURI);

            // Continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in CustomHeaderFilter for path: {}", requestURI, e);
            sendErrorResponse(response, "Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if the request URI should be excluded from header validation
     */
    private boolean shouldExcludeFromHeaderValidation(String requestURI) {
        if (requestURI == null) {
            return false;
        }

        // Check against all excluded paths
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestURI.startsWith(excludedPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Send error response in consistent format
     */
    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse apiResponse = new ApiResponse(false, message, "");
        apiResponse.setDetails(new HashMap<>());

        try {
            String jsonResponse = convertObjectToJson(apiResponse);
            response.getWriter().write(jsonResponse);
        } catch (JsonProcessingException e) {
            logger.error("Error converting response to JSON", e);
            response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
        }
    }

    /**
     * Convert object to JSON string
     */
    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}