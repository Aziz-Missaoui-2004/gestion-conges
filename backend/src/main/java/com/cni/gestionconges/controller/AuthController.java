package com.cni.gestionconges.controller;

import com.cni.gestionconges.dto.LoginRequest;
import com.cni.gestionconges.dto.LoginResponse;
import com.cni.gestionconges.entity.User;
import com.cni.gestionconges.repositories.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "Email ou mot de passe incorrect")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Email ou mot de passe incorrect");
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestion-conges")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        JwsHeader header = JwsHeader
                .with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }
}
