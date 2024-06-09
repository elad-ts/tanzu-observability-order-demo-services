package com.tutorials.app.core.ctrls;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
public class PaymentController {

    private final Counter paymentCounter;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public PaymentController(MeterRegistry meterRegistry) {
        this.paymentCounter = meterRegistry.counter("payment.counter");
    }

    @PostMapping("/payment")
    public String processPayment(String payment) {
        paymentCounter.increment();

        URI uri = UriComponentsBuilder.fromUriString("https://api.exchangerate-api.com/v4/latest/USD").build().toUri();
        CurrencyConversionResponse response = restTemplate.getForObject(uri, CurrencyConversionResponse.class);

        if (response != null && response.getRates().containsKey("ILS")) {
            double usdToIlsRate = response.getRates().get("ILS");
            System.out.println("USD to ILS conversion rate: " + usdToIlsRate);
        } else {
            throw new RuntimeException("Failed to get USD to ILS conversion rate");
        }
        return "Payment processed: " + payment;
    }
}

class CurrencyConversionResponse {
    private String base;
    private Map<String, Double> rates;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}
