package io.github.mksfilmoteka.media.auth

import io.github.mksfilmoteka.media.util.TestUtil.adminJwt
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(SecurityConfigTest.TestController::class)
@Import(SecurityConfig::class, KeycloakRealmRoleConverter::class, SecurityConfigTest.TestController::class)
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should allow public get`() {
        mockMvc.perform(get("/api/v1/test"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should reject unauthenticated write`() {
        mockMvc.perform(post("/api/v1/test"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject user write`() {
        mockMvc.perform(
            post("/api/v1/test")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow admin write`() {
        mockMvc.perform(
            post("/api/v1/test")
                .with(adminJwt())
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should allow swagger ui and api docs`() {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk)
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk)
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk)
        mockMvc.perform(get("/api-docs/media")).andExpect(status().isOk)
    }

    @RestController
    class TestController {

        @GetMapping("/api/v1/test")
        fun get(): String = "ok"

        @PostMapping("/api/v1/test")
        fun post(): String = "ok"

        @GetMapping(value = ["/swagger-ui.html", "/swagger-ui/index.html", "/api-docs", "/api-docs/{group}"])
        fun swagger(): String = "ok"
    }
}
