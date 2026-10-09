package com.example.supplychain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class DeliveryCalculatorTest {
    private final DeliveryCalculator calculator = new DeliveryCalculator(
            Clock.fixed(Instant.parse("2026-10-09T00:00:00Z"), ZoneId.of("UTC")));

    @Test
    void undeliveredPastExpectedDateIsDelayed() {
        CustomerOrder order = new CustomerOrder();
        order.setStatus(OrderStatus.SHIPPED);
        order.setExpectedDeliveryDate(LocalDate.of(2026, 10, 7));

        assertThat(calculator.isDelayed(order)).isTrue();
        assertThat(calculator.delayDays(order)).isEqualTo(2);
    }

    @Test
    void deliveredAfterExpectedDateIsDelayed() {
        CustomerOrder order = new CustomerOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setExpectedDeliveryDate(LocalDate.of(2026, 10, 7));
        order.setActualDeliveryDate(LocalDate.of(2026, 10, 10));

        assertThat(calculator.isDelayed(order)).isTrue();
        assertThat(calculator.delayDays(order)).isEqualTo(3);
    }

    @Test
    void missingExpectedDateIsNotDelayed() {
        CustomerOrder order = new CustomerOrder();
        order.setStatus(OrderStatus.SHIPPED);

        assertThat(calculator.isDelayed(order)).isFalse();
        assertThat(calculator.delayDays(order)).isNull();
    }
}
