package com.github.humbertodosreis.resiliency.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.humbertodosreis.resiliency.server.config.BehaviorManager;

@RestController
@RequestMapping("/server")
public class AppController {

    @Autowired private BehaviorManager behaviorManager;

    @GetMapping("/unreliable-endpoint")
    public String getUnreliableData() throws Exception {
        behaviorManager.applyBehavior(); // Aplica o modo atual
        return "Operação concluída com sucesso!";
    }
}
