
package com.swiftyeats.orderservice;

import com.swiftyeats.orderservice.model.User;
import com.swiftyeats.orderservice.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
    @Autowired
    private OrderStatusController orderStatusController;


@RestController
@RequestMapping("/api")
public class OrderController {
    private final Map<String, Order> orders = new HashMap<>();

    // Simple in-memory user store for demo: username -> [password, role]
    private static final Map<String, String[]> users = new HashMap<>();
    static {
        users.put("testuser", new String[]{"password123", "USER"});
        users.put("alice", new String[]{"alicepass", "ADMIN"});
    }

    // --- AUTH ---
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> resp = new HashMap<>();
        String username = user.getUsername();
        String password = user.getPassword();
        if (users.containsKey(username) && users.get(username)[0].equals(password)) {
            String role = users.get(username)[1];
            String token = JwtUtil.generateToken(username, role);
            resp.put("token", token);
            resp.put("username", username);
            resp.put("role", role);
            resp.put("success", true);
        } else {
            resp.put("success", false);
            resp.put("error", "Invalid credentials");
        }
        return resp;
    }

    // --- ORDERS ---

    @PostMapping("/orders/place")
    public Object placeOrder(@RequestBody Order order, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Collections.singletonMap("error", "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String username;
        String role;
        try {
            username = JwtUtil.extractUsername(token);
            role = JwtUtil.extractRole(token);
        } catch (Exception e) {
            return Collections.singletonMap("error", "Invalid token");
        }
        if (!"USER".equals(role)) {
            return Collections.singletonMap("error", "Only users with role USER can place orders");
        }
        String id = UUID.randomUUID().toString();
        order.setId(id);
        order.setStatus("PLACED");
        order.setUser(username);
        orders.put(id, order);
        // TODO: Publish ORDER_CREATED event to Kafka here

        // Trigger automated delivery status changes
        orderStatusController.automateOrderStatus(id);

        return order;
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable String id) {
        return orders.get(id);
    }

    @GetMapping("/orders")
    public Object getAllOrders(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Collections.singletonMap("error", "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String role;
        try {
            role = JwtUtil.extractRole(token);
        } catch (Exception e) {
            return Collections.singletonMap("error", "Invalid token");
        }
        if (!"ADMIN".equals(role)) {
            return Collections.singletonMap("error", "Only ADMINs can view all orders");
        }
        return new ArrayList<>(orders.values());
    }
}

class Order {
    private String id;
    private String user;
    private String item;
    private int quantity;
    private String status;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
