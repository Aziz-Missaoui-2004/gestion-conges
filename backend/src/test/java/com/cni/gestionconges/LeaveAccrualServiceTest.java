package com.cni.gestionconges;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.service.LeaveAccrualService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaveAccrualServiceTest {

    private final LeaveAccrualService service = new LeaveAccrualService();

    @Test
    void acquiresTwoAndHalfDaysForEachStartedMonth() {
        Agent agent = agentHiredOn(LocalDate.of(2026, 1, 15));

        LeaveAccrualService.AccrualSummary summary = service.calculate(
                agent,
                2026,
                LocalDate.of(2026, 8, 11));

        assertEquals(8, summary.monthsAcquired());
        assertEquals(20.0, summary.daysAcquired());
    }

    @Test
    void capsAnnualAcquisitionAtThirtyDays() {
        Agent agent = agentHiredOn(LocalDate.of(2025, 1, 1));

        LeaveAccrualService.AccrualSummary summary = service.calculate(
                agent,
                2026,
                LocalDate.of(2026, 12, 31));

        assertEquals(12, summary.monthsAcquired());
        assertEquals(30.0, summary.daysAcquired());
    }

    @Test
    void doesNotAcquireDaysForARelevantFutureYear() {
        Agent agent = agentHiredOn(LocalDate.of(2026, 1, 1));

        LeaveAccrualService.AccrualSummary summary = service.calculate(
                agent,
                2027,
                LocalDate.of(2026, 8, 11));

        assertEquals(0, summary.monthsAcquired());
        assertEquals(0.0, summary.daysAcquired());
    }

    @Test
    void countsCalendarDaysSinceHiring() {
        Agent agent = agentHiredOn(LocalDate.of(2026, 8, 1));

        LeaveAccrualService.AccrualSummary summary = service.calculate(
                agent,
                2026,
                LocalDate.of(2026, 8, 11));

        assertEquals(11, summary.workedDays());
    }

    private Agent agentHiredOn(LocalDate date) {
        Agent agent = new Agent();
        agent.setDateEmbauche(date);
        return agent;
    }
}
