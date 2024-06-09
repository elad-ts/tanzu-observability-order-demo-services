package com.tutorials.app.order.bl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Profile("order")
@Service
public class OrdersManager {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${delivery.service.url}")
    private String deliveryServiceUrl;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${order.service.request.interval}")
    private long orderServiceRequestInterval;

    private final Counter orderCounter;
    private final ScheduledExecutorService scheduler;

    private static final String[] PRODUCTS = {"ProductA", "ProductB", "ProductC"};
    private static final String[] PAYMENTS = {"CREDIT_CARD", "PAYPAL", "BANK_TRANSFER"};
    private static final String[] DELIVERIES = {"STANDARD", "EXPRESS", "SAME_DAY"};

    private final Random random = new Random();

    public OrdersManager(MeterRegistry meterRegistry) {
        this.orderCounter = meterRegistry.counter("orders.counter");
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @PostConstruct
    public void init() {
        scheduleNextOrder();
    }

    public void generateOrder() {
        String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
        String payment = PAYMENTS[random.nextInt(PAYMENTS.length)];
        String delivery = DELIVERIES[random.nextInt(DELIVERIES.length)];

        ResponseEntity<String> inventoryResponse = restTemplate.exchange(inventoryServiceUrl, HttpMethod.POST, new HttpEntity<>(product), String.class);
        if (inventoryResponse.getStatusCode().is2xxSuccessful()) {
            ResponseEntity<String> paymentResponse = restTemplate.exchange(paymentServiceUrl, HttpMethod.POST, new HttpEntity<>(payment), String.class);
            ResponseEntity<String> deliveryResponse = restTemplate.exchange(deliveryServiceUrl, HttpMethod.POST, new HttpEntity<>(delivery), String.class);

            if (paymentResponse.getStatusCode().is2xxSuccessful() && deliveryResponse.getStatusCode().is2xxSuccessful()) {
                Order order = new Order();
                order.setId(random.nextLong());
                order.setProduct(product);
                order.setPayment(payment);
                order.setDelivery(delivery);
                orderRepository.save(order);
            }
        }

        orderCounter.increment();
        scheduleNextOrder();
    }

    private void scheduleNextOrder() {
        long nextDelay = calculateNextDelay();
        scheduler.schedule(this::generateOrder, nextDelay, TimeUnit.MILLISECONDS);
    }

    private long calculateNextDelay() {
        return orderServiceRequestInterval + random.nextInt((int) orderServiceRequestInterval);
    }
}
