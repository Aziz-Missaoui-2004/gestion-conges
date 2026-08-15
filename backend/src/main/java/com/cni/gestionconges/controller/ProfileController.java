package com.cni.gestionconges.controller;

import com.cni.gestionconges.dto.ProfileResponse;
import com.cni.gestionconges.dto.UpdateProfileRequest;
import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.User;
import com.cni.gestionconges.repositories.AgentRepository;
import com.cni.gestionconges.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final JwtEncoder jwtEncoder;

    public ProfileController(UserRepository userRepository,
                             AgentRepository agentRepository,
                             JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.jwtEncoder = jwtEncoder;
    }

    @GetMapping("/me")
    public ProfileResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = getUser(jwt.getSubject());
        return toResponse(user, null);
    }

    @PutMapping("/me")
    @Transactional
    public ProfileResponse updateProfile(@RequestBody UpdateProfileRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        User user = getUser(jwt.getSubject());
        String nom = clean(request.getNom());
        String prenom = clean(request.getPrenom());
        String email = clean(request.getEmail());
        String telephone = clean(request.getTelephone());

        if (nom == null || prenom == null || email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom, le prénom et l'adresse e-mail sont obligatoires");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'adresse e-mail est invalide");
        }
        if (telephone != null) {
            String normalizedTelephone = telephone.replaceAll("[\\s().-]", "");
            if (!normalizedTelephone.matches("^\\+[1-9]\\d{7,14}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le numéro de téléphone doit respecter le format international");
            }
        }
        if (!email.equalsIgnoreCase(user.getEmail())
                && userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette adresse e-mail est déjà utilisée");
        }

        Agent agent = agentRepository.findByUser_Email(user.getEmail()).orElse(null);
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setTelephone(telephone);

        if (agent != null) {
            agent.setNom(nom);
            agent.setPrenom(prenom);
            agent.setTelephone(telephone);
            agentRepository.save(agent);
        }
        userRepository.save(user);

        return toResponse(user, createToken(user));
    }

    private ProfileResponse toResponse(User user, String token) {
        Agent agent = agentRepository.findByUser_Email(user.getEmail()).orElse(null);
        String nom = agent != null ? agent.getNom() : user.getNom();
        String prenom = agent != null ? agent.getPrenom() : user.getPrenom();
        String telephone = agent != null ? agent.getTelephone() : user.getTelephone();
        return new ProfileResponse(user.getId(), nom, prenom, user.getEmail(), telephone, token);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String createToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestion-conges")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
