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

		private final JwtService jwtService;
		private final AccessTokenCookieService accessTokenCookieService;

		public JwtAuthFilter(JwtService jwtService, AccessTokenCookieService accessTokenCookieService) {
				this.jwtService = jwtService;
				this.accessTokenCookieService = accessTokenCookieService;
		}

		@Override
		protected void doFilterInternal(
						HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
						throws ServletException, IOException {
				accessTokenCookieService
								.resolveToken(request)
								.filter(jwtService::isValid)
								.ifPresent(
												token -> {
														String username = jwtService.extractUsername(token);
														UsernamePasswordAuthenticationToken authentication =
																		new UsernamePasswordAuthenticationToken(
																						username, null, List.of());
														authentication.setDetails(
																		new WebAuthenticationDetailsSource().buildDetails(request));
														SecurityContextHolder.getContext().setAuthentication(authentication);
												});
				filterChain.doFilter(request, response);
		}
}
