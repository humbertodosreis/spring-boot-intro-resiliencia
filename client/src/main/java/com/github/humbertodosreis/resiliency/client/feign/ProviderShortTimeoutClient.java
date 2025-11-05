package com.github.humbertodosreis.resiliency.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "providerShortClient")
public interface ProviderShortTimeoutClient {
    @GetMapping("/server/unreliable-endpoint") String getData();
}