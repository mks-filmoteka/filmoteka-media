# Filmoteka Media

Small media service for Filmoteka.

It stores poster images on local disk and returns them by file name.

## Tech

- Kotlin
- Java 25
- Spring Boot
- Spring Security with Keycloak JWT authentication
- Spring Kafka
- Thumbnailator, Imageio
- OpenAPI / Swagger
- JUnit

## Run locally

Requires JDK 25. Kafka and Keycloak can be started using the [shared Docker Compose setup](https://github.com/mks-filmoteka/filmoteka).

Set the environment variables. These examples match the default local setup:

```powershell
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
$env:AUTH_ISSUER_URI = "http://localhost:8180/realms/filmoteka"
$env:AUTH_JWK_SET_URI = "http://localhost:8180/realms/filmoteka/protocol/openid-connect/certs"
$env:AUTH_AUDIENCE = "filmoteka-api"
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
$env:KAFKA_FILM_DELETED_TOPIC = "filmoteka.film-deleted.v1"
$env:KAFKA_FILM_POSTER_CHANGED_TOPIC = "filmoteka.film-poster-changed.v1"
$env:KAFKA_MEDIA_CONSUMER_GROUP_ID = "filmoteka-media"
$env:KAFKA_AUTO_OFFSET_RESET = "earliest"
```

For bash:

```bash
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
export AUTH_ISSUER_URI="http://localhost:8180/realms/filmoteka"
export AUTH_JWK_SET_URI="http://localhost:8180/realms/filmoteka/protocol/openid-connect/certs"
export AUTH_AUDIENCE="filmoteka-api"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
export KAFKA_FILM_DELETED_TOPIC="filmoteka.film-deleted.v1"
export KAFKA_FILM_POSTER_CHANGED_TOPIC="filmoteka.film-poster-changed.v1"
export KAFKA_MEDIA_CONSUMER_GROUP_ID="filmoteka-media"
export KAFKA_AUTO_OFFSET_RESET="earliest"
```

For IDE runs, set these variables in the run configuration.

Start the app:

```bash
./gradlew bootRun
```

On Windows, replace `./gradlew` with `.\gradlew.bat`.

App runs on:

```text
http://localhost:8081
```

Check readiness at [http://localhost:8081/actuator/health/readiness](http://localhost:8081/actuator/health/readiness).
Stop the app with `Ctrl+C`, or the IDE's Stop button.

Uploaded files are stored in:

```text
uploads/
```

This can be changed in `application.yaml`:

```yaml
media:
  root-location: uploads
```

The same setting can be supplied through `MEDIA_ROOT_LOCATION`.

## API docs

```text
http://localhost:8081/swagger-ui/index.html
```

## Main endpoints

```text
POST   /api/v1/media/files
GET    /api/v1/media/files/{fileName}
DELETE /api/v1/media/files/{fileName}
```

Reading posters is public. Uploading and deleting require a Keycloak access token with the `ADMIN` realm role, sent as `Authorization: Bearer <token>`.

## Upload

Multipart request field name:

```text
file
```

Supported formats:

```text
jpg
jpeg
png
webp
```

The file and total multipart request limits are both 5 MB.

Images are resized before saving.

Default size:

```text
max width: 300
max height: 450
```

## Kafka cleanup

Film-deletion events remove the film's poster. Poster-change events remove the previous poster after replacement or removal. Cleanup is asynchronous, and deleting an already missing file succeeds.

Failed cleanup uses retry topics and then `.media.dlt` topics. Dead-letter records are retained for 30 days and require manual investigation; automatic DLT processing is disabled.

## Build

Build the application and run the tests:

```bash
./gradlew clean build
```

JARs are written to `build/libs/`; the executable JAR has no `-plain` suffix.
Remove generated build files with `./gradlew clean`. Uploaded posters are kept.

## Tests

```bash
./gradlew test
```

Kafka integration tests start an embedded broker.

## Notes

- File names are generated with UUID.
- Service checks image extension and actual image content.
- Local storage only for now.
- Health details, `/actuator/info` and `/actuator/metrics` require the `ADMIN` role.