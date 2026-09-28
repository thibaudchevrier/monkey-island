# Monkey Island game server.
# The jar is platform independent: build it natively, even for a multi-arch image.
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Gradle and the plugins first: this layer stays cached until the build changes.
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet help

COPY config config
COPY src src
ARG VERSION=0.0.0-SNAPSHOT
# Tests and quality checks run in CI (./gradlew check); the image only needs the jar.
RUN ./gradlew --no-daemon --quiet -Pversion=${VERSION} jar

FROM eclipse-temurin:25-jre
RUN groupadd --system monkey && useradd --system --gid monkey --no-create-home monkey
WORKDIR /app
COPY --from=build /build/build/libs/monkey-island.jar .
USER monkey
EXPOSE 13579
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "monkey-island.jar"]
