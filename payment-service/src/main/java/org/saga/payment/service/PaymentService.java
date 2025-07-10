package org.saga.payment.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.saga.common.dto.OrderRequestDto;
import org.saga.common.dto.PaymentRequestDto;
import org.saga.common.event.OrderEvent;
import org.saga.common.event.OrderStatus;
import org.saga.common.event.PaymentEvent;
import org.saga.common.event.PaymentStatus;
import org.saga.payment.entity.UserBalance;
import org.saga.payment.entity.UserTransaction;
import org.saga.payment.repository.UserBalanceRepository;
import org.saga.payment.repository.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @PostConstruct
    private void initUserBalanceInDB() {
        userBalanceRepository.saveAll(
                Stream.of(
                        new UserBalance(100, 3000),
                        new UserBalance(101, 5000),
                        new UserBalance(102, 2000),
                        new UserBalance(103, 3800)
                ).collect(Collectors.toList())
        );
    }

    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();

        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                orderRequestDto.getUserId(),
                orderRequestDto.getOrderId(),
                orderRequestDto.getAmount()
        );

        return userBalanceRepository.findById(orderRequestDto.getUserId())
                .filter(userBalance -> userBalance.getAmount() > orderRequestDto.getAmount())
                .map(
                        userBalance -> {
                            userBalance.setAmount(userBalance.getAmount() - orderRequestDto.getAmount());
                            userBalanceRepository.save(userBalance);

                            userTransactionRepository.save(
                                    new UserTransaction(
                                            orderRequestDto.getOrderId(),
                                            orderRequestDto.getUserId(),
                                            orderRequestDto.getAmount())
                            );

                            return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                        }
                )
                .orElse(new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED));
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        userTransactionRepository.findById(orderEvent.getOrderRequestDto().getOrderId())
                .ifPresent(userTransaction -> {
                    userTransactionRepository.delete(userTransaction);
                    userBalanceRepository.findById(orderEvent.getOrderRequestDto().getUserId())
                            .ifPresent(orderBalance -> {
                                orderBalance.setAmount(orderBalance.getAmount() + userTransaction.getPrice());
                                userBalanceRepository.save(orderBalance);
                            });
                });
    }
}
