package com.pm.ordersystem.client;

import com.pm.ordersystem.engine.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

    private final TriagingEngine triagingEngine;
    private final PriorityFirstStrategy priorityFirstStrategy;
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final DeadlineFirstStrategy deadlineFirstStrategy;

    public StrategyController(TriagingEngine triagingEngine,
                              PriorityFirstStrategy priorityFirstStrategy,
                              LoadBalancingStrategy loadBalancingStrategy,
                              DeadlineFirstStrategy deadlineFirstStrategy) {
        this.triagingEngine = triagingEngine;
        this.priorityFirstStrategy = priorityFirstStrategy;
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.deadlineFirstStrategy = deadlineFirstStrategy;
    }

    @GetMapping
    public ResponseEntity<?> getStrategy() {
        return ResponseEntity.ok(Map.of("strategy", triagingEngine.getStrategyName()));
    }

    @PostMapping
    public ResponseEntity<?> setStrategy(@RequestBody Map<String, String> body) {
        String name = body.get("strategy");

        TriageStrategy selected = switch (name) {
            case "loadBalancing", "LoadBalancingStrategy" -> loadBalancingStrategy;
            case "deadlineFirst", "DeadlineFirstStrategy" -> deadlineFirstStrategy;
            default -> priorityFirstStrategy;
        };

        triagingEngine.setStrategy(selected);
        return ResponseEntity.ok(Map.of("strategy", triagingEngine.getStrategyName()));
    }
}