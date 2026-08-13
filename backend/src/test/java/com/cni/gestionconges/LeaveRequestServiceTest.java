package com.cni.gestionconges;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveBalance;
import com.cni.gestionconges.entity.LeaveRequest;
import com.cni.gestionconges.entity.LeaveStatus;
import com.cni.gestionconges.entity.Role;
import com.cni.gestionconges.entity.User;
import com.cni.gestionconges.entity.Validation;
import com.cni.gestionconges.entity.ValidationDecision;
import com.cni.gestionconges.repositories.LeaveBalanceRepository;
import com.cni.gestionconges.repositories.LeaveRequestRepository;
import com.cni.gestionconges.repositories.ValidationRepository;
import com.cni.gestionconges.service.LeaveAccrualService;
import com.cni.gestionconges.service.LeaveRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LeaveRequestServiceTest {

    private LeaveRequestRepository requestRepository;
    private LeaveBalanceRepository balanceRepository;
    private ValidationRepository validationRepository;
    private LeaveRequestService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(LeaveRequestRepository.class);
        balanceRepository = mock(LeaveBalanceRepository.class);
        validationRepository = mock(ValidationRepository.class);
        service = new LeaveRequestService(
                requestRepository,
                balanceRepository,
                validationRepository,
                new LeaveAccrualService());
        when(requestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(validationRepository.save(any(Validation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void intermediateApprovalKeepsRequestPendingAndMovesToNextManager() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now().withMonth(1).withDayOfMonth(1));
        Agent firstManager = agent(2L, Role.RESPONSABLE, null);
        Agent finalManager = agent(3L, Role.RESPONSABLE, null);
        firstManager.setResponsableDirect(finalManager);

        LeaveRequest request = request(employee, firstManager, 1);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));

        service.approve(10L, firstManager, "Avis favorable");

        assertEquals(LeaveStatus.EN_ATTENTE, request.getStatut());
        assertEquals(finalManager, request.getProchainValidateur());
        assertEquals(2, request.getNiveauValidation());
        verify(validationRepository).save(argThat(validation ->
                validation.getValidateur().equals(firstManager)
                        && validation.getDecision() == ValidationDecision.APPROUVEE
                        && validation.getNiveau() == 1));
        verify(balanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    void finalApprovalValidatesRequestAndConsumesBalanceOnce() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now().withMonth(1).withDayOfMonth(1));
        Agent finalManager = agent(2L, Role.RESPONSABLE, null);
        LeaveRequest request = request(employee, finalManager, 1);
        LeaveBalance balance = new LeaveBalance();
        balance.setAgent(employee);
        balance.setAnnee(LocalDate.now().getYear());
        balance.setJoursAcquis(30.0);
        balance.setJoursConsommes(0.0);

        when(requestRepository.findById(11L)).thenReturn(Optional.of(request));
        when(balanceRepository.findByAgentAndAnnee(employee, LocalDate.now().getYear()))
                .thenReturn(Optional.of(balance));

        service.approve(11L, finalManager, null);

        assertEquals(LeaveStatus.VALIDEE, request.getStatut());
        assertEquals(1.0, balance.getJoursConsommes());
        assertEquals(null, request.getProchainValidateur());
        verify(balanceRepository).save(balance);
    }

    @Test
    void rejectionStopsWorkflowWithoutConsumingBalance() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now());
        Agent manager = agent(2L, Role.RESPONSABLE, null);
        LeaveRequest request = request(employee, manager, 1);
        when(requestRepository.findById(12L)).thenReturn(Optional.of(request));

        service.reject(12L, manager, "Période non compatible");

        assertEquals(LeaveStatus.REFUSEE, request.getStatut());
        verify(balanceRepository, never()).save(any(LeaveBalance.class));
        verify(validationRepository).save(argThat(validation ->
                validation.getDecision() == ValidationDecision.REFUSEE));
    }

    @Test
    void finalApprovalFailsWhenMonthlyBalanceIsInsufficient() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now());
        Agent manager = agent(2L, Role.RESPONSABLE, null);
        LeaveRequest request = request(employee, manager, 5);
        LeaveBalance balance = new LeaveBalance();
        balance.setAgent(employee);
        balance.setAnnee(LocalDate.now().getYear());
        balance.setJoursAcquis(0.0);
        balance.setJoursConsommes(0.0);

        when(requestRepository.findById(13L)).thenReturn(Optional.of(request));
        when(balanceRepository.findByAgentAndAnnee(employee, LocalDate.now().getYear()))
                .thenReturn(Optional.of(balance));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.approve(13L, manager, null));
        assertEquals(LeaveStatus.EN_ATTENTE, request.getStatut());
    }

    @Test
    void creationBalanceCheckRejectsRequestAboveCurrentMonthlyAcquisition() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now());
        when(balanceRepository.findByAgentAndAnnee(employee, LocalDate.now().getYear()))
                .thenReturn(Optional.empty());
        when(requestRepository.findByAgentAndStatut(employee, LeaveStatus.EN_ATTENTE))
                .thenReturn(List.of());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.ensureAvailableBalance(
                        employee,
                        LocalDate.now().getYear(),
                        3));
    }

    @Test
    void creationBalanceCheckReservesPendingRequests() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now());
        LeaveRequest pending = request(employee, agent(2L, Role.RESPONSABLE, null), 2);
        when(balanceRepository.findByAgentAndAnnee(employee, LocalDate.now().getYear()))
                .thenReturn(Optional.empty());
        when(requestRepository.findByAgentAndStatut(employee, LeaveStatus.EN_ATTENTE))
                .thenReturn(List.of(pending));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.ensureAvailableBalance(
                        employee,
                        LocalDate.now().getYear(),
                        1));
    }

    @Test
    void creationBalanceCheckAllowsRequestWithinAvailableBalance() throws Exception {
        Agent employee = agent(1L, Role.AGENT, LocalDate.now());
        when(balanceRepository.findByAgentAndAnnee(employee, LocalDate.now().getYear()))
                .thenReturn(Optional.empty());
        when(requestRepository.findByAgentAndStatut(employee, LeaveStatus.EN_ATTENTE))
                .thenReturn(List.of());

        service.ensureAvailableBalance(
                employee,
                LocalDate.now().getYear(),
                2);

        verify(requestRepository).findByAgentAndStatut(employee, LeaveStatus.EN_ATTENTE);
    }

    private LeaveRequest request(Agent employee, Agent validator, int days) {
        LeaveRequest request = new LeaveRequest();
        request.setAgent(employee);
        request.setDateDebut(LocalDate.now());
        request.setDateFin(LocalDate.now().plusDays(days - 1L));
        request.setNombreJours(days);
        request.setStatut(LeaveStatus.EN_ATTENTE);
        request.setProchainValidateur(validator);
        request.setNiveauValidation(1);
        return request;
    }

    private Agent agent(Long id, Role role, LocalDate hireDate) throws Exception {
        Agent agent = new Agent();
        setId(agent, id);
        agent.setUser(new User("user" + id + "@test.local", "password", role));
        agent.setDateEmbauche(hireDate);
        return agent;
    }

    private void setId(Agent agent, Long id) throws Exception {
        Field field = Agent.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(agent, id);
    }
}
