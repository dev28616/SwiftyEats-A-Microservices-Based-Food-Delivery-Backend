# Order Service

This service manages all order-related operations for SwiftyEats.

## Features

- REST APIs for creating, updating, and fetching orders
- Publishes `ORDER_CREATED` events to Kafka
- Integrates with ETA service via REST
- **Real-time order status updates via WebSocket**

## Build & Run

### Build with Gradle

```
./gradlew build
```

### Run locally

```
./gradlew bootRun
```

### Docker

Build the JAR and then build the Docker image:

```
./gradlew bootJar
docker build -t swiftyeats-order-service .
```

## Environment

- Java 11
- Spring Boot 2.7.x
- Kafka

## Real-Time Order Tracking (WebSocket)

- WebSocket endpoint: `ws://<host>:8081/ws-orders`
- STOMP topic: `/topic/order-status`
- Example client subscription:

```
const socket = new SockJS('http://localhost:8081/ws-orders');
const stompClient = Stomp.over(socket);
stompClient.connect({}, function(frame) {
	stompClient.subscribe('/topic/order-status', function(message) {
		console.log(JSON.parse(message.body));
	});
});
```

- Order status updates are broadcast automatically when the `/api/orders/status/update` endpoint is called with:

```
{
	"orderId": "123",
	"status": "DELIVERED",
	"message": "Order delivered successfully."
}
```
