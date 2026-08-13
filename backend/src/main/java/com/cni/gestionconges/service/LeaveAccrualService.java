package com.cni.gestionconges.service;

import com.cni.gestionconges.entity.Agent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LeaveAccrualService {

    public AccrualSummary calculate(Agent agent, int year, LocalDate date) {
        LocalDate hireDate = agent.getDateEmbauche() == null
                ? date
                : agent.getDateEmbauche();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate accrualStart = hireDate.isAfter(yearStart) ? hireDate : yearStart;

        if (accrualStart.isAfter(date)) {
            return new AccrualSummary(0, 0.0, 0);
        }

        LocalDate firstMonth = accrualStart.withDayOfMonth(1);
        LocalDate currentMonth = date.withDayOfMonth(1);
        int months = (int) ChronoUnit.MONTHS.between(firstMonth, currentMonth) + 1;
        double acquired = Math.min(months * 2.5, 30.0);

        long workedDays = ChronoUnit.DAYS.between(hireDate, date) + 1;
        return new AccrualSummary(months, acquired, Math.max(workedDays, 0));
    }

    public record AccrualSummary(
            int monthsAcquired,
            double daysAcquired,
            long workedDays) {
    }
}
