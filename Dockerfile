# 1단계: Spring Boot 빌드
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Windows CRLF 문제 방지 + 실행 권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src

RUN ./gradlew clean bootJar --no-daemon


# 2단계: 실제 실행 이미지
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]