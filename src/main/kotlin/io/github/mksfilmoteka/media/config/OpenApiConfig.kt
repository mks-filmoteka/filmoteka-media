package io.github.mksfilmoteka.media.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun filmotekaMediaOpenApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Filmoteka Media API")
                    .version("v1")
                    .description("API for uploading, loading, and deleting media files.")
            )
    }
}
