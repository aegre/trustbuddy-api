FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw \
	&& ./mvnw dependency:go-offline -B -q

COPY src src

RUN ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build /app/target/trustbuddy-api-*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
