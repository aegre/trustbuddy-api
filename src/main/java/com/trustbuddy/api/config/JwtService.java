package com.trustbuddy.api.config;

import com.trustbuddy.api.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

		private final JwtProperties jwtProperties;
		private volatile SecretKey signingKey;

		public JwtService(JwtProperties jwtProperties) {
				this.jwtProperties = jwtProperties;
		}

		public String generateToken(String subject) {
				Instant now = Instant.now();
				return Jwts.builder()
								.subject(subject)
								.issuedAt(Date.from(now))
								.expiration(Date.from(now.plusMillis(jwtProperties.expirationMs())))
								.signWith(signingKey())
								.compact();
		}

		public String extractUsername(String token) {
				return parseClaims(token).getSubject();
		}

		public boolean isValid(String token) {
				try {
						parseClaims(token);
						return true;
				} catch (JwtException exception) {
						return false;
				}
		}

		private Claims parseClaims(String token) {
				return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
		}

		private SecretKey signingKey() {
				SecretKey key = signingKey;
				if (key == null) {
						key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
						signingKey = key;
				}
				return key;
		}
}
