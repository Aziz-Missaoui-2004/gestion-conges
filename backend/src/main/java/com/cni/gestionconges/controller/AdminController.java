package com.cni.gestionconges.controller;

import com.cni.gestionconges.dto.CreateAgentRequest;
import com.cni.gestionconges.dto.UpdateManagerRequest;
import com.cni.gestionconges.dto.UpdateLeaveBalanceRequest;
import com.cni.gestionconges.dto.UpdateAgentStatusRequest;
import com.cni.gestionconges.entity.*;
import com.cni.gestionconges.repositories.AgentRepository;
import com.cni.gestionconges.repositories.LeaveBalanceRepository;
import com.cni.gestionconges.repositories.LeaveRequestRepository;
import com.cni.gestionconges.repositories.ServiceRepository;
import com.cni.gestionconges.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            AgentRepository agentRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            PasswordEncoder passwordEncoder) {

        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/agents")
    public List<Agent> getAgents() {
        return agentRepository.findAll();
    }

    @PostMapping("/agents")
    @Transactional
    public Agent createAgent(
            @RequestBody CreateAgentRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cet email existe déjà"
            );
        }

        com.cni.gestionconges.entity.Service service =
                serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Service introuvable"
                                )
                        );

        User user = new User();

        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        Role role = request.getRole() == null ? Role.AGENT : request.getRole();
        if (role == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La création d'un administrateur n'est pas disponible ici");
        }
        user.setRole(role);

        user = userRepository.save(user);

        Agent agent = new Agent();

        agent.setNom(request.getNom());
        agent.setPrenom(request.getPrenom());
        agent.setUser(user);
        agent.setService(service);
        agent.setDateEmbauche(LocalDate.now());

        if (request.getResponsableDirectId() != null) {
            Agent responsable = agentRepository.findById(request.getResponsableDirectId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Responsable introuvable"));
            if (responsable.getUser().getRole() != Role.RESPONSABLE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'agent sélectionné n'est pas un responsable");
            }
            agent.setResponsableDirect(responsable);
        }

        agent = agentRepository.save(agent);

        LeaveBalance balance = new LeaveBalance();

        balance.setAgent(agent);
        balance.setAnnee(LocalDate.now().getYear());
        balance.setJoursAcquis(0.0);
        balance.setJoursConsommes(0.0);

        leaveBalanceRepository.save(balance);

        return agent;
    }

    @PutMapping("/agents/{id}/manager")
    @Transactional
    public Agent updateManager(
            @PathVariable Long id,
            @RequestBody UpdateManagerRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));

        if (request.getResponsableDirectId() == null) {
            if (!leaveRequestRepository.findByAgentAndStatut(agent, LeaveStatus.EN_ATTENTE).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Impossible de retirer le responsable : des demandes sont encore en attente");
            }
            agent.setResponsableDirect(null);
            return agentRepository.save(agent);
        }

        Agent responsable = agentRepository.findById(request.getResponsableDirectId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Responsable introuvable"));
        if (agent.getId().equals(responsable.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un agent ne peut pas être son propre responsable");
        }
        if (responsable.getUser().getRole() != Role.RESPONSABLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'agent sélectionné n'est pas un responsable");
        }
        ensureNoHierarchyCycle(agent, responsable);
        agent.setResponsableDirect(responsable);
        Agent savedAgent = agentRepository.save(agent);

        leaveRequestRepository.findByAgentAndStatut(savedAgent, LeaveStatus.EN_ATTENTE)
                .forEach(pendingRequest -> {
                    pendingRequest.setProchainValidateur(responsable);
                    pendingRequest.setNiveauValidation(1);
                    leaveRequestRepository.save(pendingRequest);
                });

        return savedAgent;
    }

    @PutMapping("/agents/{id}/status")
    public Agent updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateAgentStatusRequest request) {
        if (request.getStatut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le statut de l'agent est obligatoire");
        }
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));
        agent.setStatut(request.getStatut());
        return agentRepository.save(agent);
    }

    @PutMapping("/agents/{id}/balance")
    public LeaveBalance updateBalanceAdjustment(
            @PathVariable Long id,
            @RequestBody UpdateLeaveBalanceRequest request) {
        if (request.getAnnee() == null || request.getJoursAjustement() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'année et l'ajustement sont obligatoires");
        }
        if (request.getJoursAjustement() < -30 || request.getJoursAjustement() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'ajustement doit être compris entre -30 et 30 jours");
        }
        if (request.getAnnee() > LocalDate.now().getYear()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Les soldes des années futures ne sont pas disponibles");
        }

        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));
        LeaveBalance balance = leaveBalanceRepository.findByAgentAndAnnee(agent, request.getAnnee())
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setAgent(agent);
                    newBalance.setAnnee(request.getAnnee());
                    newBalance.setJoursAcquis(0.0);
                    newBalance.setJoursConsommes(0.0);
                    return newBalance;
                });
        balance.setJoursAjustement(request.getJoursAjustement());
        return leaveBalanceRepository.save(balance);
    }

    private void ensureNoHierarchyCycle(Agent agent, Agent responsable) {
        Agent current = responsable;
        while (current != null) {
            if (current.getId().equals(agent.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cette relation créerait une boucle hiérarchique");
            }
            current = current.getResponsableDirect();
        }
    }

    @GetMapping("/services")
    public List<com.cni.gestionconges.entity.Service> getServices() {
        return serviceRepository.findAll();
    }

    @PostMapping("/services")
    public com.cni.gestionconges.entity.Service createService(
            @RequestBody com.cni.gestionconges.entity.Service service) {

        if (service.getNom() == null || service.getNom().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom du service est obligatoire"
            );
        }

        String serviceName = service.getNom().trim();
        if (serviceRepository.existsByNomIgnoreCase(serviceName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un service portant ce nom existe déjà");
        }
        service.setNom(serviceName);

        return serviceRepository.save(service);
    }
}
