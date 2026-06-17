package com.river.sand.service;

import com.river.sand.entity.ForbiddenPeriod;
import com.river.sand.repository.ForbiddenPeriodRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ForbiddenPeriodService {

    private final ForbiddenPeriodRepository forbiddenPeriodRepository;
    private static final DateTimeFormatter MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    public ForbiddenPeriodService(ForbiddenPeriodRepository forbiddenPeriodRepository) {
        this.forbiddenPeriodRepository = forbiddenPeriodRepository;
    }

    public List<ForbiddenPeriod> getAllEnabledPeriods() {
        return forbiddenPeriodRepository.findByEnabled(true);
    }

    public boolean isInForbiddenPeriod(LocalDate date) {
        List<ForbiddenPeriod> periods = getAllEnabledPeriods();
        String monthDay = date.format(MONTH_DAY_FORMAT);
        int currentYear = date.getYear();

        for (ForbiddenPeriod period : periods) {
            if (period.getYear() != null && !period.getYear().equals(currentYear)) {
                continue;
            }
            String start = period.getStartMonthDay();
            String end = period.getEndMonthDay();
            if (isBetween(monthDay, start, end)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasForbiddenPeriodOverlap(LocalDate startDate, LocalDate endDate) {
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (isInForbiddenPeriod(date)) {
                return true;
            }
            date = date.plusDays(1);
        }
        return false;
    }

    public ForbiddenPeriod getForbiddenPeriodForDate(LocalDate date) {
        List<ForbiddenPeriod> periods = getAllEnabledPeriods();
        String monthDay = date.format(MONTH_DAY_FORMAT);
        int currentYear = date.getYear();

        for (ForbiddenPeriod period : periods) {
            if (period.getYear() != null && !period.getYear().equals(currentYear)) {
                continue;
            }
            if (isBetween(monthDay, period.getStartMonthDay(), period.getEndMonthDay())) {
                return period;
            }
        }
        return null;
    }

    private boolean isBetween(String target, String start, String end) {
        if (start.compareTo(end) <= 0) {
            return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
        } else {
            return target.compareTo(start) >= 0 || target.compareTo(end) <= 0;
        }
    }
}
