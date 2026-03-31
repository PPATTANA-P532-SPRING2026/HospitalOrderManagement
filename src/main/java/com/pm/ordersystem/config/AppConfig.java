package com.pm.ordersystem.config;

import com.pm.ordersystem.engine.TriageStrategy;
import com.pm.ordersystem.engine.PriorityFirstStrategy;
import com.pm.ordersystem.handler.AuditLoggingDecorator;
import com.pm.ordersystem.handler.BaseOrderHandler;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.handler.PriorityBoostDecorator;
import com.pm.ordersystem.handler.ValidationDecorator;
import com.pm.ordersystem.notification.ConsoleNotificationService;
import com.pm.ordersystem.notification.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

    //  Triage Strategy
    @Bean
    public TriageStrategy triageStrategy() {
        return new PriorityFirstStrategy();
    }

    //  Notification Service
    @Bean
    public NotificationService notificationService() {
        return new ConsoleNotificationService();
    }

    // Decorator Chain

    @Bean
    public OrderHandler orderHandler(BaseOrderHandler base) {
        OrderHandler chain = base;
        chain = new AuditLoggingDecorator(chain);
        chain = new PriorityBoostDecorator(chain);
        chain = new ValidationDecorator(chain);
        return chain;
    }

    //  CORS
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