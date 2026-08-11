package io.github.mksfilmoteka.media.auth

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration(proxyBeanMethods = false)
class SecurityConfig {

    companion object {
        private const val URL_PATTERN = "/api/v1/**"
        private const val ADMIN_ROLE = "ADMIN"
        private val SWAGGER_PATHS = arrayOf("/swagger-ui/**", "/swagger-ui.html", "/api-docs", "/api-docs/**")
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter
    ): SecurityFilterChain {
        return http
            .cors(withDefaults())
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
                it.requestMatchers(*SWAGGER_PATHS).permitAll()
                it.requestMatchers(HttpMethod.GET, URL_PATTERN).permitAll()
                it.requestMatchers(HttpMethod.POST, URL_PATTERN).hasRole(ADMIN_ROLE)
                it.requestMatchers(HttpMethod.DELETE, URL_PATTERN).hasRole(ADMIN_ROLE)
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter) } }
            .build()
    }

    @Bean
    fun jwtAuthenticationConverter(
        realmRoleConverter: KeycloakRealmRoleConverter
    ): JwtAuthenticationConverter {
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(realmRoleConverter)
        }
    }
}