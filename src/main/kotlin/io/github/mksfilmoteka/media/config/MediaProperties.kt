package io.github.mksfilmoteka.media.config

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "media")
data class MediaProperties(
    @field:NotBlank
    val rootLocation: String = "uploads",

    @field:Valid
    val image: ImageProperties = ImageProperties()
)

data class ImageProperties(
    @field:Min(1)
    val maxWidth: Int = 300,

    @field:Min(1)
    val maxHeight: Int = 450,

    @field:DecimalMin("0.1")
    @field:DecimalMax("1.0")
    val outputQuality: Double = 0.9
)