package com.trustbuddy.api.testsupport;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresRedisTestcontainers {

		@Container @ServiceConnection
		protected static final PostgreSQLContainer POSTGRES =
						new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

		@Container @ServiceConnection
		protected static final RedisContainer REDIS =
						new RedisContainer(DockerImageName.parse("redis:7-alpine"));
}
