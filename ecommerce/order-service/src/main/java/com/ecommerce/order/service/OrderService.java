package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.kafka.OrderEventProducer;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public OrderDto.OrderResponse createOrder(OrderDto.CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
            .map(i -> OrderItem.builder()
                .productId(i.getProductId())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .build())
            .collect(Collectors.toList());

        BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .customerId(request.getCustomerId())
            .totalAmount(total)
            .build();

        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);
        log.info("Created order: {}", saved.getOrderNumber());

        // Publish Kafka event asynchronously
        eventProducer.publishOrderCreated(OrderDto.OrderCreatedEvent.builder()
            .orderNumber(saved.getOrderNumber())
            .customerId(saved.getCustomerId())
            .totalAmount(saved.getTotalAmount())
            .createdAt(saved.getCreatedAt())
            .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getOrder(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getOrderByNumber(String orderNumber) {
        return toResponse(orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderNumber)));
    }

    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderDto.OrderResponse cancelOrder(Long id) {
        Order order = findById(id);
        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has been shipped or delivered");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        eventProducer.publishOrderCancelled(saved.getOrderNumber());
        log.info("Cancelled order: {}", saved.getOrderNumber());
        return toResponse(saved);
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + date + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderDto.OrderResponse toResponse(Order order) {
        List<OrderDto.OrderItemResponse> itemResponses = order.getItems().stream()
            .map(i -> OrderDto.OrderItemResponse.builder()
                .productId(i.getProductId())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .subtotal(i.getSubtotal())
                .build())
            .collect(Collectors.toList());

        return OrderDto.OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .status(order.getStatus())
            .items(itemResponses)
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
}
