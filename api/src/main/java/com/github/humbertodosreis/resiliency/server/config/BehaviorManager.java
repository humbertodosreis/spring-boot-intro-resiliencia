package com.github.humbertodosreis.resiliency.server.config;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class BehaviorManager {
    private volatile BehaviorMode mode = BehaviorMode.HEALTHY;
    private volatile int delayMs = 0;
    private volatile int failFirstNAttempts = 0;
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    public enum BehaviorMode { HEALTHY, SLOW, FLAKY }

    public void setHealthy() { setMode(BehaviorMode.HEALTHY, 0, 0); }
    public void setSlow(int delay) { setMode(BehaviorMode.SLOW, delay, 0); }
    public void setFlaky(int count) { setMode(BehaviorMode.FLAKY, 0, count); }

    private void setMode(BehaviorMode mode, int delay, int failCount) {
        this.mode = mode;
        this.delayMs = delay;
        this.failFirstNAttempts = failCount;
        this.requestCounter.set(0);
    }

    public void applyBehavior() throws InterruptedException {
        if (mode == BehaviorMode.SLOW && delayMs > 0) {
            Thread.sleep(delayMs);
        }
        if (mode == BehaviorMode.FLAKY) {
            int attempt = requestCounter.incrementAndGet();
            if (attempt <= failFirstNAttempts) {
                System.out.println("PROVIDER: Simulando falha (Tentativa " + attempt + ")");
                throw new RuntimeException("Falha simulada");
            }
        }
    }
}
