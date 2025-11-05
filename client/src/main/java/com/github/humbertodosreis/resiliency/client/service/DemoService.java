package com.github.humbertodosreis.resiliency.client.service;

import com.github.humbertodosreis.resiliency.client.feign.ProviderShortTimeoutClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;

@Service
public class DemoService {
  // Usamos o client de timeout curto como padrão para os retries
  @Autowired
  private ProviderShortTimeoutClient apiClient;

  // 1. Sem Resiliência
  public String callNoRetry() {
    return apiClient.getData();
  }

  // 2. Retry Simples
  @Retryable(maxAttempts = 3, value = FeignException.class)
  public String callSimpleRetry() {
    System.out.println("[Cliente] Tentando com Retry Simples...");
    return apiClient.getData();
  }

  // 3. Com Backoff
  @Retryable(maxAttempts = 4, value = FeignException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
  public String callWithBackoff() {
    System.out.println("[Cliente] Tentando com Backoff...");
    return apiClient.getData();
  }

  // 4. Com Jitter
  @Retryable(maxAttempts = 5, value = FeignException.class, backoff = @Backoff(delay = 1000, multiplier = 2, random = true))
  public String callWithJitter() {
    System.out.println("[Cliente] Tentando com Jitter...");
    return apiClient.getData();
  }
}