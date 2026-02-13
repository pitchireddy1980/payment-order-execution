# Payment Order and Execution System

A comprehensive Spring Boot 3 application for managing payment orders and their execution lifecycle using clean layered architecture.

## 🏗️ Architecture

This application follows a clean **layered architecture** pattern:

```
┌─────────────────────────────────────┐
│     Controller Layer (REST API)      │  ← REST endpoints, request/response handling
├─────────────────────────────────────┤
│        Service Layer                 │  ← Business logic, transaction management
├─────────────────────────────────────┤
│       Repository Layer               │  ← Data access, JPA repositories
├─────────────────────────────────────┤
│        Entity Layer                  │  ← Domain models, database entities
└─────────────────────────────────────┘
```

### Layer Responsibilities

- **Controller Layer**: HTTP request handling, input validation, response formatting
- **Service Layer**: Business logic, orchestration, transaction management
- **Repository Layer**: Database operations, query execution
- **Entity Layer**: Domain models, database mappings

## 📋 Features

- ✅ Create and manage payment orders
- ✅ Execute payments through payment gateway
- ✅ Track payment execution status
- ✅ Retry failed payments
- ✅ Settlement and reversal operations
- ✅ Comprehensive query capabilities
- ✅ RESTful API design
- ✅ Swagger API documentation
- ✅ Global exception handling
- ✅ Input validation
- ✅ Transaction management

## 🛠️ Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: Database operations
- **H2 Database**: In-memory database (dev/test)
- **PostgreSQL**: Production database support
- **Lombok**: Boilerplate code reduction
- **ModelMapper**: DTO-Entity mapping
- **Springdoc OpenAPI**: API documentation (Swagger)
- **Maven**: Build and dependency management

## 📊 Database Schema

### Payment Orders Table
```sql
payment_orders (
    id BIGINT PRIMARY KEY,
    order_reference VARCHAR(255) UNIQUE,
    customer_id VARCHAR(255),
    customer_name VARCHAR(255),
    customer_email VARCHAR(255),
    amount DECIMAL(19,2),
    currency VARCHAR(3),
    status VARCHAR(50),
    payment_method VARCHAR(50),
    description VARCHAR(1000),
    beneficiary_name VARCHAR(255),
    beneficiary_account VARCHAR(255),
    beneficiary_bank VARCHAR(255),
    beneficiary_bank_code VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP
)
```

### Payment Executions Table
```sql
payment_executions (
    id BIGINT PRIMARY KEY,
    execution_reference VARCHAR(255) UNIQUE,
    payment_order_id BIGINT,
    status VARCHAR(50),
    amount DECIMAL(19,2),
    currency VARCHAR(3),
    gateway_transaction_id VARCHAR(255),
    gateway_provider VARCHAR(255),
    retry_attempt INTEGER,
    error_message VARCHAR(2000),
    error_code VARCHAR(100),
    gateway_response VARCHAR(2000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    processed_at TIMESTAMP,
    settled_at TIMESTAMP,
    remarks VARCHAR(1000),
    FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id)
)
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd payment-system
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### Accessing H2 Console

URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:paymentdb`
- Username: `sa`
- Password: (leave blank)

### Accessing Swagger UI

URL: `http://localhost:8080/api/swagger-ui.html`

## 📡 API Endpoints

### Payment Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/payment-orders` | Create a new payment order |
| GET | `/v1/payment-orders/{id}` | Get order by ID |
| GET | `/v1/payment-orders/reference/{ref}` | Get order by reference |
| GET | `/v1/payment-orders` | Get all orders |
| GET | `/v1/payment-orders/customer/{id}` | Get orders by customer |
| GET | `/v1/payment-orders/status/{status}` | Get orders by status |
| GET | `/v1/payment-orders/date-range` | Get orders by date range |
| GET | `/v1/payment-orders/amount-range` | Get orders by amount range |
| PUT | `/v1/payment-orders/{id}` | Update order |
| PATCH | `/v1/payment-orders/{id}/status` | Update order status |
| PATCH | `/v1/payment-orders/{id}/cancel` | Cancel order |
| DELETE | `/v1/payment-orders/{id}` | Delete order |
| GET | `/v1/payment-orders/customer/{id}/stats` | Get customer statistics |

### Payment Executions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/payment-executions/execute/{orderId}` | Execute payment |
| GET | `/v1/payment-executions/{id}` | Get execution by ID |
| GET | `/v1/payment-executions/reference/{ref}` | Get execution by reference |
| GET | `/v1/payment-executions/order/{orderId}` | Get executions by order |
| GET | `/v1/payment-executions/order-reference/{ref}` | Get executions by order ref |
| GET | `/v1/payment-executions/status/{status}` | Get executions by status |
| GET | `/v1/payment-executions/customer/{id}` | Get executions by customer |
| PATCH | `/v1/payment-executions/{id}/status` | Update execution status |
| POST | `/v1/payment-executions/{id}/retry` | Retry failed execution |
| POST | `/v1/payment-executions/{id}/settle` | Process settlement |
| POST | `/v1/payment-executions/{id}/reverse` | Reverse execution |

## 📝 API Usage Examples

### Create Payment Order

```bash
curl -X POST http://localhost:8080/api/v1/payment-orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "amount": 1000.00,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "description": "Payment for Order #12345",
    "beneficiaryName": "ABC Corp",
    "beneficiaryAccount": "1234567890",
    "beneficiaryBank": "XYZ Bank",
    "beneficiaryBankCode": "XYZ001"
  }'
```

### Execute Payment

```bash
curl -X POST http://localhost:8080/api/v1/payment-executions/execute/1
```

### Get Order by Reference

```bash
curl http://localhost:8080/api/v1/payment-orders/reference/ORD-ABC123
```

### Get Orders by Status

```bash
curl http://localhost:8080/api/v1/payment-orders/status/PENDING
```

## 🔄 Payment Order Lifecycle

```
PENDING → PROCESSING → COMPLETED
   ↓           ↓            ↓
CANCELLED   FAILED      REFUNDED
```

## 🔄 Payment Execution Lifecycle

```
INITIATED → PROCESSING → SUCCESS → SETTLED
                ↓           ↓
             FAILED    REVERSED
```

## 🏗️ Project Structure

```
payment-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/paymentsystem/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── service/             # Business logic
│   │   │   │   └── impl/            # Service implementations
│   │   │   └── PaymentSystemApplication.java
│   │   └── resources/
│   │       └── application.yml      # Application configuration
│   └── test/                        # Test classes
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## ⚙️ Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:h2:mem:paymentdb
    driver-class-name: org.h2.Driver
    
payment:
  execution:
    retry-attempts: 3
    timeout-seconds: 30
  order:
    max-amount: 1000000
    min-amount: 0.01
```

## 🧪 Testing

Run tests using Maven:

```bash
mvn test
```

## 🔐 Security Considerations

- Add Spring Security for authentication/authorization
- Implement JWT token-based authentication
- Add rate limiting for API endpoints
- Encrypt sensitive data (payment details)
- Implement audit logging
- Add HTTPS/TLS support

## 🚀 Production Deployment

### PostgreSQL Configuration

Update `application.yml` for production:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Environment Variables

Set these environment variables:

- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `GATEWAY_API_KEY`: Payment gateway API key
- `GATEWAY_SECRET`: Payment gateway secret

## 📈 Future Enhancements

- [ ] Add payment gateway integrations (Stripe, PayPal, etc.)
- [ ] Implement scheduled payment processing
- [ ] Add webhook support for gateway notifications
- [ ] Implement payment refund workflows
- [ ] Add comprehensive audit logging
- [ ] Implement real-time payment status updates (WebSocket)
- [ ] Add payment analytics and reporting
- [ ] Implement multi-currency support
- [ ] Add batch payment processing
- [ ] Implement payment reconciliation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Contact

For questions or support, please contact:
- Email: support@paymentsystem.com
- Website: https://www.paymentsystem.com
