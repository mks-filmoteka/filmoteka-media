package io.github.mksfilmoteka.media.auth

import io.github.mksfilmoteka.media.util.TestUtil.testJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KeycloakRealmRoleConverterTest {

    private val converter = KeycloakRealmRoleConverter()

    @Test
    fun `should convert scopes and realm roles`() {
        val jwt = testJwt(
            mapOf(
                "scope" to "openid profile",
                "realm_access" to mapOf("roles" to listOf("USER", "ADMIN"))
            )
        )

        val result = converter.convert(jwt)

        assertThat(result.map { it.authority })
            .containsExactlyInAnyOrder("SCOPE_openid", "SCOPE_profile", "ROLE_USER", "ROLE_ADMIN")
    }

    @Test
    fun `should return scope authorities when realm access is missing`() {
        val jwt = testJwt(mapOf("scope" to "openid profile"))

        val result = converter.convert(jwt)

        assertThat(result.map { it.authority })
            .containsExactlyInAnyOrder("SCOPE_openid", "SCOPE_profile")
    }

    @Test
    fun `should return scope authorities when roles claim is missing`() {
        val jwt = testJwt(mapOf("scope" to "openid", "realm_access" to emptyMap<String, Any>()))

        val result = converter.convert(jwt)

        assertThat(result.map { it.authority })
            .containsExactly("SCOPE_openid")
    }
}
