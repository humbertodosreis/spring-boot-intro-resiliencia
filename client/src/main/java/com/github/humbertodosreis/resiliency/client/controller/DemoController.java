package com.github.humbertodosreis.resiliency.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.humbertodosreis.resiliency.client.feign.ProviderLongTimeoutClient;
import com.github.humbertodosreis.resiliency.client.feign.ProviderShortTimeoutClient;
import com.github.humbertodosreis.resiliency.client.service.DemoService;

@RestController
public class DemoController {
    @Autowired private DemoService demoService;
    @Autowired private ProviderShortTimeoutClient shortClient;
    @Autowired private ProviderLongTimeoutClient longClient;

    @GetMapping("/demo/timeout")
    public String runTimeoutDemo(@RequestParam String strategy) {
        return "short".equals(strategy) ?
            shortClient.getData() : longClient.getData();
    }

    @GetMapping("/demo/retry")
    public String runRetryDemo(@RequestParam String strategy) {
        return switch (strategy.toLowerCase()) {
            case "simple" -> demoService.callSimpleRetry();
            case "backoff" -> demoService.callWithBackoff();
            case "jitter" -> demoService.callWithJitter();
            default -> demoService.callNoRetry();
        };
    }

    @GetMapping("/health")
    public String healthCheck() { return "Estou vivo!"; }
}
