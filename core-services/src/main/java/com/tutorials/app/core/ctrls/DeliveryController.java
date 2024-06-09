package com.tutorials.app.core.ctrls;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("delivery")
@RestController
public class DeliveryController {

    private final AtomicInteger storageFreeSpace;
    private final Integer storageSize = 100;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask;

    @Autowired
    public DeliveryController(MeterRegistry meterRegistry) {
        this.storageFreeSpace = new AtomicInteger(storageSize);
        Gauge.builder("delivery.storage.free-space", storageFreeSpace, AtomicInteger::get)
                .description("Storage free space of the delivery service")
                .register(meterRegistry);
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(1);
        startRegularPattern(1000);
    }

    @PostMapping("/delivery")
    public String processDelivery(String delivery) {
        return "Delivery processed: " + delivery;
    }

    @GetMapping("/slowness")
    public String deliverySlowness(@RequestParam String type, @RequestParam long totalTimeMillis, @RequestParam long delayIntervalMillis) {
        cancelCurrentTask();

        switch (type) {
            case "regular":
                startRegularPattern(delayIntervalMillis);
                break;
            case "random":
                currentTask = scheduler.schedule(() -> simulateRandomPattern(totalTimeMillis, delayIntervalMillis), 0, TimeUnit.MILLISECONDS);
                break;
            case "linear":
                currentTask = scheduler.schedule(() -> simulateLinearPattern(totalTimeMillis, delayIntervalMillis), 0, TimeUnit.MILLISECONDS);
                break;
            case "exponential":
                currentTask = scheduler.schedule(() -> simulateExponentialPattern(totalTimeMillis, delayIntervalMillis), 0, TimeUnit.MILLISECONDS);
                break;
            default:
                return "Invalid type. Use 'regular', 'random', 'linear', or 'exponential'.";
        }
        return "Simulated delivery slowness with " + type + " pattern over " + totalTimeMillis + " milliseconds.";
    }

    private void cancelCurrentTask() {
        if (currentTask != null && !currentTask.isDone()) {
            try {
            currentTask.cancel(true);
            while (!currentTask.isCancelled() && !currentTask.isDone()) {
                Thread.sleep(100); // Briefly sleep to give time for the task to cancel
            }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startRegularPattern(long delayIntervalMillis) {
        double resetLevel = 0.999 * storageSize;
        updateStorageFreeSpace((int) resetLevel, "regular reset",delayIntervalMillis);

        currentTask = scheduler.scheduleAtFixedRate(() -> {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            int currentFreeSpace = storageFreeSpace.get();
            double maxDiskUsedSpace = 0.85 * storageSize;
            if (currentFreeSpace < maxDiskUsedSpace) {
                updateStorageFreeSpace((int) resetLevel, "regular reset" ,delayIntervalMillis);
            } else {
                updateStorageFreeSpace(currentFreeSpace - (int) (storageSize * 0.01), "regular decrement" ,delayIntervalMillis);
            }
        }, 0, delayIntervalMillis, TimeUnit.MILLISECONDS);
    }

    private void simulateRandomPattern(long totalTimeMillis, long delayIntervalMillis) {
        long iterations = totalTimeMillis / delayIntervalMillis;
        for (int i = 0; i < iterations; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            int newFreeSpace = random.nextInt((int) (0.8 * storageSize)); // Random free space between 0 and 30% of storageSize
            updateStorageFreeSpace(newFreeSpace, "random spike" ,delayIntervalMillis);
            simulateDelay(delayIntervalMillis);
        }
        startRegularPattern(delayIntervalMillis);
    }

    private void simulateLinearPattern(long totalTimeMillis, long delayIntervalMillis) {
        long iterations = totalTimeMillis / delayIntervalMillis;
        double decrement = (0.50 * storageSize) / iterations;
        double resetLevel = 0.20 * storageSize;

        for (int i = 0; i < iterations; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            int newFreeSpace = (int) (storageFreeSpace.get() - decrement);
            updateStorageFreeSpace(newFreeSpace, "linear decrease" ,delayIntervalMillis);
            simulateDelay(delayIntervalMillis);
        }
        updateStorageFreeSpace((int) resetLevel, "linear reset" ,delayIntervalMillis);
        startRegularPattern(delayIntervalMillis);
    }

    private void simulateExponentialPattern(long totalTimeMillis, long delayIntervalMillis) {
        long iterations = totalTimeMillis / delayIntervalMillis;
        double factor = Math.pow(0.95, 1.0 / iterations);

        for (int i = 0; i < iterations; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            int newFreeSpace = (int) (storageFreeSpace.get() * factor);
            updateStorageFreeSpace(newFreeSpace, "exponential decrease" ,delayIntervalMillis);
            simulateDelay(delayIntervalMillis);
        }
        startRegularPattern(delayIntervalMillis);
    }

    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateStorageFreeSpace(int newValue, String simulationType, long delayIntervalMillis) {
        storageFreeSpace.set(newValue);
        double utilization = 100.0 * (storageSize - newValue) / storageSize;
        System.out.println("Storage free space updated to: " + newValue + " by " + simulationType + ". Current utilization: " + utilization + "%, Wait time: " + delayIntervalMillis + " ms");
    }
}
