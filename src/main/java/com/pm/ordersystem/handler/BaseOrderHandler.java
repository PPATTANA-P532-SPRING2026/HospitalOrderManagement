package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;
@Component
public class BaseOrderHandler implements OrderHandler {

    @Override
    public void handle(Order order) {
        //end of the chain
        // order has already passed through all decorators

        System.out.println("[HANDLER] Order " + order.getId()
                + " processed by BaseOrderHandler");
    }
}