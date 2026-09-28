# Monkey Island game server.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
# The legacy test suite has known failures (see README), so tests are run separately.
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/monkey-island.jar .
EXPOSE 13579
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "monkey-island.jar"]
