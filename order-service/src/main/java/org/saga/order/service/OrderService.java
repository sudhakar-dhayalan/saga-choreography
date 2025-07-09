package org.saga.order.service;

import jakarta.transaction.Transactional;
import org.saga.common.dto.OrderRequestDto;
import org.saga.common.event.OrderStatus;
import org.saga.order.entity.PurchaseOrder;
import org.saga.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusPublisher orderStatusPublisher;

    @Transactional
    public PurchaseOrder createOrder(OrderRequestDto orderRequestDto) {
        PurchaseOrder order = orderRepository.save(convertDtoToEntity(orderRequestDto));
        orderRequestDto.setOrderId(order.getId());

        // produce kafka event with status ORDER_CREATED
        orderStatusPublisher.publishOrderEvent(orderRequestDto, OrderStatus.ORDER_CREATED);
        return order;
    }

    public List<PurchaseOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    private PurchaseOrder convertDtoToEntity(OrderRequestDto orderRequestDto) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPrice(orderRequestDto.getAmount());
        purchaseOrder.setUserId(orderRequestDto.getUserId());
        purchaseOrder.setProductId(orderRequestDto.getProductId());
        purchaseOrder.setOrderStatus(OrderStatus.ORDER_CREATED);

        return purchaseOrder;
    }
}
