package com.pm.ordersystem.config;

import com.pm.ordersystem.engine.PriorityFirstStrategy;
import com.pm.ordersystem.engine.TriageStrategy;
import com.pm.ordersystem.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Autowired
    private PriorityFirstStrategy priorityFirstStrategy;

    // ── Clock — injected into DeadlineFirstStrategy and
    //            PriorityEscalationDecorator for testability ──────────
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // ── Primary triage strategy ───────────────────────────────────────
    @Bean
    public TriageStrategy triageStrategy() {
        return priorityFirstStrategy;
    }

    // ── Decorator chain ───────────────────────────────────────────────
    // Order: Validation → PriorityEscalation → StatAudit → PriorityBoost
    //        → AuditLogging → Base
    @Bean
    public OrderHandler orderHandler(BaseOrderHandler base,
                                     Clock clock,
                                     com.pm.ordersystem.access.OrderAccess orderAccess,
                                     com.pm.ordersystem.command.CommandLog commandLog) {
        OrderHandler chain = base;
        chain = new AuditLoggingDecorator(chain);
        chain = new PriorityBoostDecorator(chain);
        chain = new StatAuditDecorator(chain, commandLog);
        chain = new PriorityEscalationDecorator(chain, clock, orderAccess);
        chain = new ValidationDecorator(chain);
        return chain;
    }

    // ── CORS ──────────────────────────────────────────────────────────
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT",
                                "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}