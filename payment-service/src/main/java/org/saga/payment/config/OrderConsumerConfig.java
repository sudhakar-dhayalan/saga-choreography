package org.saga.payment.config;

import org.saga.common.event.OrderEvent;
import org.saga.common.event.OrderStatus;
import org.saga.common.event.PaymentEvent;
import org.saga.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class OrderConsumerConfig {

    @Autowired
    private PaymentService paymentService;

    @Bean
    public Function<Flux<OrderEvent>, Flux<PaymentEvent>> paymentProcessor() {
        return orderEventFlux -> orderEventFlux.flatMap(this::processPayment);
    }

    private Mono<PaymentEvent> processPayment(OrderEvent orderEvent) {
        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
            return Mono.fromSupplier(() -> paymentService.newOrderEvent(orderEvent));
        } else {
            return Mono.fromRunnable((() -> paymentService.cancelOrderEvent(orderEvent)));
        }
    }
}
