package com.pm.ordersystem.config;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.engine.PriorityFirstStrategy;
import com.pm.ordersystem.engine.TriageStrategy;
import com.pm.ordersystem.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Autowired
    private PriorityFirstStrategy priorityFirstStrategy;

    // ── Clock bean
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // ── Default Triage Strategy
    @Bean
    @Primary
    public TriageStrategy triageStrategy() {
        return priorityFirstStrategy;
    }

    // ── Decorator Chain
    // Week 1: Validation → PriorityBoost → AuditLogging → Base
    // Week 2: Validation → PriorityEscalation → StatAudit
    //         → PriorityBoost → AuditLogging → Base
    @Bean
    public OrderHandler orderHandler(BaseOrderHandler base,
                                     OrderAccess orderAccess,
                                     Clock clock) {
        OrderHandler chain = base;
        chain = new AuditLoggingDecorator(chain);
        chain = new PriorityBoostDecorator(chain);
        chain = new StatAuditDecorator(chain);
        chain = new PriorityEscalationDecorator(
                chain, orderAccess, clock);
        chain = new ValidationDecorator(chain);
        return chain;
    }

    // ── CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST",
                                "PUT", "DELETE");
            }
        };
    }
}