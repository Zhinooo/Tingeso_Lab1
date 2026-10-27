FROM docker.io/library/eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM docker.io/library/eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/build/libs/*.jar app.jar
USER app
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
