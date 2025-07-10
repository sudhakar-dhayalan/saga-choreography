package org.saga.common.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.saga.common.dto.PaymentRequestDto;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentEvent implements Event {
    private final UUID eventId = UUID.randomUUID();
    private final Date orderDate = new Date();
    private PaymentRequestDto orderRequestDto;
    private PaymentStatus orderStatus;

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Date getDate() {
        return orderDate;
    }

    public PaymentEvent(PaymentRequestDto orderRequestDto, PaymentStatus orderStatus) {
        this.orderRequestDto = orderRequestDto;
        this.orderStatus = orderStatus;
    }
}
