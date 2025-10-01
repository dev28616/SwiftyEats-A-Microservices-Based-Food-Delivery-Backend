package com.swiftyeats.orderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@RestController
@Configuration
@EnableAsync
    // Simulate automated delivery status progression for an order
    @PostMapping("/api/orders/status/automate/{orderId}")
    public void automateOrderStatus(@PathVariable String orderId) {
        automateStatusAsync(orderId);
    }

    @Async
    public void automateStatusAsync(String orderId) {
        try {
            String[] stages = {"ACCEPTED", "PREPARING", "PICKED_UP", "ON_THE_WAY", "DELIVERED"};
            String[] messages = {"Order accepted by restaurant", "Food is being prepared", "Picked up by delivery partner", "On the way to you", "Delivered! Enjoy your meal"};
            for (int i = 0; i < stages.length; i++) {
                OrderStatusUpdate update = new OrderStatusUpdate();
                update.setOrderId(orderId);
                update.setStatus(stages[i]);
                update.setMessage(messages[i]);
                messagingTemplate.convertAndSend("/topic/order-status", update);
                TimeUnit.SECONDS.sleep(2); // Simulate delay between stages
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
public class OrderStatusController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Call this endpoint to broadcast order status updates
    @PostMapping("/api/orders/status/update")
    public void updateOrderStatus(@RequestBody OrderStatusUpdate update) {
        messagingTemplate.convertAndSend("/topic/order-status", update);
    }

    // For WebSocket clients to send messages (optional)
    @MessageMapping("/order-status")
    public void receiveStatusUpdate(OrderStatusUpdate update) {
        messagingTemplate.convertAndSend("/topic/order-status", update);
    }
}

class OrderStatusUpdate {
    private String orderId;
    private String status;
    private String message;

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
