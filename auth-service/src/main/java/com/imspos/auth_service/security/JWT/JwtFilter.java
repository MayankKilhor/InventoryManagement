package com.imspos.auth_service.security.JWT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imspos.auth_service.payload.response.ApiErrorResponse;
import com.imspos.auth_service.security.UserPrincipal;
import com.imspos.auth_service.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String requestPath = request.getRequestURI();

		// Skip JWT validation for Swagger/OpenAPI endpoints
		if (isSwaggerEndpoint(requestPath)) {
			chain.doFilter(request, response);
			return;
		}

		final String requestTokenHeader = request.getHeader("Authorization");
		String username = null;
		String jwtToken = null;

		try {
			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
				jwtToken = requestTokenHeader.substring(7);
				try {
					username = jwtTokenUtil.extractUsername(jwtToken);
				} catch (IllegalArgumentException e) {
					logger.error("Unable to get JWT Token", e);
					sendErrorResponse(response, "Unable to get JWT Token", HttpServletResponse.SC_BAD_REQUEST);
					return;
				} catch (ExpiredJwtException e) {
					logger.error("JWT Token has expired", e);
					sendErrorResponse(response, "JWT Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				} catch (JwtException e) {
					logger.error("Invalid JWT Token", e);
					sendErrorResponse(response, "Invalid JWT Token", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			} else {
				// Only log warning for non-public endpoints
				if (!isPublicEndpoint(requestPath)) {
					logger.warn("JWT Token does not begin with Bearer String for path: " + requestPath);
					sendErrorResponse(response, "Authorization header missing or invalid", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			}

			// Validate token and set authentication
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserPrincipal userDetails = this.authService.loadUserByUsername(username);

				if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
							new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				} else {
					logger.warn("JWT Token is not valid");
					sendErrorResponse(response, "JWT Token is not valid", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			}

			chain.doFilter(request, response);

		} catch (UsernameNotFoundException ex) {
			logger.error("User not found: " + ex.getMessage());
			sendErrorResponse(response, "User not found: " + ex.getMessage(), HttpServletResponse.SC_NOT_FOUND);
		} catch (IllegalArgumentException e) {
			logger.error("Unable to get JWT Token", e);
			sendErrorResponse(response, "Unable to get JWT Token", HttpServletResponse.SC_BAD_REQUEST);
		} catch (ExpiredJwtException e) {
			logger.error("JWT Token has expired", e);
			sendErrorResponse(response, "JWT Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JwtException e) {
			logger.error("Invalid JWT Token", e);
			sendErrorResponse(response, "Invalid JWT Token", HttpServletResponse.SC_UNAUTHORIZED);
		} catch (Exception e) {
			logger.error("An unexpected error occurred", e);
			sendErrorResponse(response, "An internal server error occurred: " + e.getMessage(),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Check if the request path is a Swagger/OpenAPI endpoint
	 */
	private boolean isSwaggerEndpoint(String path) {
		return path.startsWith("/v3/api-docs") ||
				path.startsWith("/swagger-ui") ||
				path.startsWith("/swagger-resources") ||
				path.equals("/swagger-ui.html") ||
				path.startsWith("/webjars/");
	}

	/**
	 * Check if the request path is a public endpoint that doesn't require JWT
	 * Add your public endpoints here (login, register, etc.)
	 */
	private boolean isPublicEndpoint(String path) {
		return path.startsWith("/api/auth/login") ||
				path.startsWith("/api/auth/register") ||
				path.startsWith("/api/auth/public") ||
				path.startsWith("/actuator/health") ||
				isSwaggerEndpoint(path);
	}

	private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ApiErrorResponse errorResponse = new ApiErrorResponse(false, "Authentication error occurred!");
		errorResponse.addDetail("error", message);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResponse = objectMapper.writeValueAsString(errorResponse);

		response.getWriter().write(jsonResponse);
	}
}