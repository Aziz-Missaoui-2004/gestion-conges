package com.cni.gestionconges.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class SecurityConfig {

    // Développement uniquement.
    // On déplacera cette clé dans application.properties/env ensuite.
    private static final String SECRET =
            "gestion-conges-cni-secret-key-2026-super-securisee-123456";

    @Bean
    public SecretKey jwtSecretKey() {

        return new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(request -> {

                    CorsConfiguration config =
                            new CorsConfiguration();

                    config.setAllowedOrigins(
                            List.of("http://localhost:5173")
                    );

                    config.setAllowedMethods(
                            List.of(
                                    "GET",
                                    "POST",
                                    "PUT",
                                    "DELETE",
                                    "OPTIONS"
                            )
                    );

                    config.setAllowedHeaders(List.of("*"));

                    return config;
                }))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/leave-requests/*/approve",
                                "/api/leave-requests/*/reject"
                        )
                        .hasRole("RESPONSABLE")

                        .requestMatchers(
                                "/api/leave-requests/my",
                                "/api/leave-requests/*/cancel"
                        )
                        .hasAnyRole("AGENT", "RESPONSABLE")

                        .requestMatchers(
                                "/api/agents/me",
                                "/api/agents/me/balance/*"
                        )
                        .hasAnyRole("AGENT", "RESPONSABLE")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/leave-requests"
                        )
                        .hasAnyRole("AGENT", "RESPONSABLE")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/leave-requests"
                        )
                        .hasRole("RESPONSABLE")

                        .requestMatchers("/api/validations/my")
                        .hasRole("RESPONSABLE")

                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}
