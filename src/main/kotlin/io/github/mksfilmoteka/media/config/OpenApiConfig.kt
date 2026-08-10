package io.github.mksfilmoteka.media.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.method.HandlerMethod

@Configuration
class OpenApiConfig {

    companion object {
        private const val BEARER_AUTH = "bearerAuth"
    }

    @Bean
    fun filmotekaMediaOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Filmoteka Media API")
                    .version("v1")
                    .description("API for uploading, loading, and deleting media files.")
            )
            .components(
                Components().addSecuritySchemes(
                    BEARER_AUTH,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )

    @Bean
    fun adminWriteOperationCustomizer(): OperationCustomizer =
        OperationCustomizer { operation, handlerMethod ->
            if (isAdminWriteOperation(handlerMethod)) {
                operation.addSecurityItem(SecurityRequirement().addList(BEARER_AUTH))

                val responses = operation.responses ?: ApiResponses().also { operation.responses = it }

                addResponseIfMissing(responses, "401", "Unauthorized")
                addResponseIfMissing(responses, "403", "Forbidden - ADMIN role required")
            }
            operation
        }

    private fun isAdminWriteOperation(handlerMethod: HandlerMethod): Boolean =
        handlerMethod.hasMethodAnnotation(PostMapping::class.java) ||
                handlerMethod.hasMethodAnnotation(DeleteMapping::class.java)

    private fun addResponseIfMissing(responses: ApiResponses, responseCode: String, description: String) {
        if (!responses.containsKey(responseCode)) {
            responses.addApiResponse(responseCode, ApiResponse().description(description))
        }
    }
}
