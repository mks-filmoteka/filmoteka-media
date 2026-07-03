package io.github.mksfilmoteka.media.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "media")
data class MediaProperties(
    @field:NotBlank
    val rootLocation: String = "uploads"
)