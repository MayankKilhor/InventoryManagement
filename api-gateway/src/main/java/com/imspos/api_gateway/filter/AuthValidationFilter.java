package com.imspos.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imspos.api_gateway.payload.response.ApiErrorResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthValidationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    public AuthValidationFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private final List<String> openEndpoints = List.of(
            "/auth/login", "/auth/signup","/auth/validate"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Skip validation for open endpoints
        if (openEndpoints.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Unauthorized to do any action");
            errorResponse.addDetail("error", "Missing or invalid Authorization header");
            return writeErrorResponse(exchange, errorResponse, HttpStatus.UNAUTHORIZED);
        }else if( !authHeader.startsWith("Bearer ")) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Unauthorized to do any action");
            errorResponse.addDetail("error", "Authorization header must be present and start with 'Bearer '");
            return writeErrorResponse(exchange, errorResponse, HttpStatus.UNAUTHORIZED);
        }


        return webClientBuilder.build()
                .get()
                .uri("lb://auth-service/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .header("x-machineId", "123456")
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    Map<String, Object> claims = (Map<String, Object>) body.get("claims");
                                    if (claims == null) {
                                        return writeErrorResponse(exchange,
                                                new ApiErrorResponse(false, "Invalid token structure, missing claims"),
                                                HttpStatus.UNAUTHORIZED);
                                    }

                                    // Inject claims into request headers
                                    ServerWebExchange mutatedExchange = exchange.mutate()
                                            .request(builder -> builder.headers(headers -> {
                                                headers.add("x-user-id", String.valueOf(claims.get("userId")));
                                                headers.add("x-user-role", String.valueOf(claims.get("role")));
                                                headers.add("x-username", String.valueOf(claims.get("username")));
                                            }))
                                            .build();

                                    return chain.filter(mutatedExchange);
                                });
                    } else {
                        // Forward original error response
                        exchange.getResponse().setStatusCode(clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    DataBuffer buffer = exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(body.getBytes(StandardCharsets.UTF_8));
                                    return exchange.getResponse().writeWith(Mono.just(buffer));
                                });
                    }
                });

    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
    private Mono<Void> writeRawError(ServerWebExchange exchange, String body, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ApiErrorResponse errorResponse, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }
//update validator

}
