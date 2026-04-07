package com.pm.ordersystem.client;

import com.pm.ordersystem.engine.DeadlineFirstStrategy;
import com.pm.ordersystem.engine.LoadBalancingStrategy;
import com.pm.ordersystem.engine.PriorityFirstStrategy;
import com.pm.ordersystem.engine.TriageStrategy;
import com.pm.ordersystem.engine.TriagingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StrategyController {

    private final TriagingEngine triagingEngine;
    private final PriorityFirstStrategy priorityFirstStrategy;
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final DeadlineFirstStrategy deadlineFirstStrategy;

    public StrategyController(
            TriagingEngine triagingEngine,
            PriorityFirstStrategy priorityFirstStrategy,
            LoadBalancingStrategy loadBalancingStrategy,
            DeadlineFirstStrategy deadlineFirstStrategy) {
        this.triagingEngine        = triagingEngine;
        this.priorityFirstStrategy = priorityFirstStrategy;
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.deadlineFirstStrategy = deadlineFirstStrategy;
    }

    //  GET /api/strategy
    @GetMapping("/strategy")
    public ResponseEntity<?> getStrategy() {
        return ResponseEntity.ok(Map.of(
                "strategy", triagingEngine.getStrategyName()
        ));
    }

    //  POST /api/strategy
    @PostMapping("/strategy")
    public ResponseEntity<?> setStrategy(
            @RequestBody Map<String, String> body) {
        String name = body.get("strategy");

        TriageStrategy selected = switch (name) {
            case "loadBalancing" -> loadBalancingStrategy;
            case "deadlineFirst" -> deadlineFirstStrategy;
            default              -> priorityFirstStrategy;
        };

        triagingEngine.setStrategy(selected);

        return ResponseEntity.ok(Map.of(
                "strategy", name,
                "message",  "Strategy updated to " + name
        ));
    }
}