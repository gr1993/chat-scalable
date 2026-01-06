# ----------- STEP 1: Build Stage -----------
FROM gradle:jdk21 AS build

WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성만 미리 다운로드
RUN gradle dependencies || true

# 소스 복사
COPY . .

# JAR 빌드
RUN gradle clean build --no-daemon


# ----------- STEP 2: Run Stage -----------
FROM bellsoft/liberica-runtime-container:jdk-21-musl

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# JVM 옵션 추가
ENV JAVA_TOOL_OPTIONS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdump.hprof"

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]