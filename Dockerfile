FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon


FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system filmoteka && useradd --system --gid filmoteka --no-create-home filmoteka \
    && mkdir -p /app/uploads \
    && chown filmoteka:filmoteka /app/uploads \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build --chown=filmoteka:filmoteka /app/build/libs/*.jar /app/app.jar

USER filmoteka:filmoteka

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
