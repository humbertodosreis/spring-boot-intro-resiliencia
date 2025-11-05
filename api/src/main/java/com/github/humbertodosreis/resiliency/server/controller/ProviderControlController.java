package com.github.humbertodosreis.resiliency.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.humbertodosreis.resiliency.server.config.BehaviorManager;

@RestController
@RequestMapping("/control")
public class ProviderControlController {
  @Autowired
  private BehaviorManager behaviorManager;

  @PostMapping("/healthy")
  public String healthy() {
    behaviorManager.setHealthy();
    return "OK: HEALTHY";
  }

  @PostMapping("/slow")
  public String slow(@RequestParam int delay) {
    behaviorManager.setSlow(delay);
    return "OK: SLOW " + delay + "ms";
  }

  @PostMapping("/flaky")
  public String flaky(@RequestParam int count) {
    behaviorManager.setFlaky(count);
    return "OK: FLAKY " + count + " falhas";
  }
}
