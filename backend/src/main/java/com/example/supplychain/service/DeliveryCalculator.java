package com.example.supplychain.service;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class DeliveryCalculator {
    private final Clock clock;

    public DeliveryCalculator(Clock clock) {
        this.clock = clock;
    }

    public boolean isDelayed(CustomerOrder order) {
        return delayDays(order) != null;
    }

    public Long delayDays(CustomerOrder order) {
        LocalDate expected = order.getExpectedDeliveryDate();
        if (expected == null || order.getStatus() == OrderStatus.CANCELLED) {
            return null;
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            LocalDate actual = order.getActualDeliveryDate();
            if (actual != null && actual.isAfter(expected)) {
                return ChronoUnit.DAYS.between(expected, actual);
            }
            return null;
        }
        LocalDate today = LocalDate.now(clock);
        if (today.isAfter(expected)) {
            return ChronoUnit.DAYS.between(expected, today);
        }
        return null;
    }
}

