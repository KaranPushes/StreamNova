FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/transcoder-service-*.jar app.jar
COPY transcode.sh /transcode.sh
RUN chmod +x /transcode.sh

ENTRYPOINT ["java", "-jar", "app.jar"]