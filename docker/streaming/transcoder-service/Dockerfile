FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

COPY backend/services/transcoder-service/target/transcoder-service-*.jar app.jar
COPY docker/streaming/transcoder-service/transcode.sh /transcode.sh

RUN chmod +x /transcode.sh

ENTRYPOINT ["java", "-jar", "app.jar"]