# Filmoteka Media

Small media service for Filmoteka.

It stores poster images on local disk and returns them by file name.

## Tech

- Kotlin
- Java 25
- Spring Boot
- Thumbnailator, Imageio
- OpenAPI / Swagger
- JUnit

## Run locally

```bash
./gradlew bootRun
```

App runs on:

```text
http://localhost:8081
```

Uploaded files are stored in:

```text
uploads/
```

This can be changed in `application.yaml`:

```yaml
media:
  root-location: uploads
```

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

Images are resized before saving.

Default size:

```text
max width: 300
max height: 450
```

## Tests

```bash
./gradlew test
```

## Notes

- File names are generated with UUID.
- Service checks image extension and actual image content.
- Local storage only for now.