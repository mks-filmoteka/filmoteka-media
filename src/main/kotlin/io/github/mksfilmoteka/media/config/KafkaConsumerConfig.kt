package io.github.mksfilmoteka.media.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration(proxyBeanMethods = false)
class KafkaConsumerConfig {

    @Bean
    fun kafkaErrorHandler(): DefaultErrorHandler =
        DefaultErrorHandler(FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS))
            .apply { setClassifications(emptyMap(), true) }
}
