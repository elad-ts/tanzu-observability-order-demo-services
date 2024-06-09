package com.tutorials.app.core.ctrls;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@Profile("inventory")
@RestController
public class InventoryController {

    private final Counter inventoryCounter;
    private final Random random = new Random();

    @Autowired
    public InventoryController(MeterRegistry meterRegistry) {
        this.inventoryCounter = meterRegistry.counter("inventory.counter");
    }

    @PostMapping("/inventory")
    public String checkInventory(String product) {
        inventoryCounter.increment();
        int sleepTime = 1000 + random.nextInt(2000); // Random sleep time between 1000 and 3000 milliseconds
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        if (random.nextBoolean()) {
            System.out.println("Simulated transaction rollback for product: " + product);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated exception for product: " + product);
        }
        return random.nextBoolean() ? "OK" : "NOT_OK";
    }

}