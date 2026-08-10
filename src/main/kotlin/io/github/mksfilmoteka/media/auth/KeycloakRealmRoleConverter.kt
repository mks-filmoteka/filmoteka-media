package io.github.mksfilmoteka.media.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component
class KeycloakRealmRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    private val scopeAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = LinkedHashSet(scopeAuthoritiesConverter.convert(jwt))
        val realmAccessClaim = jwt.getClaim<Map<String, Any>>("realm_access") ?: return authorities
        val rolesClaim = realmAccessClaim["roles"] as? Collection<*> ?: return authorities
        rolesClaim
            .filterIsInstance<String>()
            .map { SimpleGrantedAuthority("ROLE_$it") }
            .forEach(authorities::add)

        return authorities
    }
}