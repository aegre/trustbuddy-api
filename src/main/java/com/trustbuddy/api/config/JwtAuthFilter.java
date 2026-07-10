package com.trustbuddy.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

		private static final String AUTHORIZATION_HEADER = "Authorization";
		private static final String BEARER_PREFIX = "Bearer ";

		private final JwtService jwtService;

		public JwtAuthFilter(JwtService jwtService) {
				this.jwtService = jwtService;
		}

		@Override
		protected void doFilterInternal(
						HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
						throws ServletException, IOException {
				String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
				if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
						String token = authorizationHeader.substring(BEARER_PREFIX.length());
						if (jwtService.isValid(token)) {
								String username = jwtService.extractUsername(token);
								UsernamePasswordAuthenticationToken authentication =
												new UsernamePasswordAuthenticationToken(username, null, List.of());
								authentication.setDetails(
												new WebAuthenticationDetailsSource().buildDetails(request));
								SecurityContextHolder.getContext().setAuthentication(authentication);
						}
				}
				filterChain.doFilter(request, response);
		}
}
