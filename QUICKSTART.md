# Payment System - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Prerequisites
- Java 17 installed
- Maven 3.6+ installed
- Your favorite IDE (IntelliJ IDEA recommended)

### Step 1: Extract and Navigate
```bash
cd payment-system
```

### Step 2: Build the Project
```bash
mvn clean install
```

### Step 3: Run the Application
```bash
mvn spring-boot:run
```

The application will start at: `http://localhost:8080/api`

### Step 4: Test the APIs

#### Open Swagger UI
Navigate to: `http://localhost:8080/api/swagger-ui.html`

#### Or Use cURL

**Create a Payment Order:**
```bash
curl -X POST http://localhost:8080/api/v1/payment-orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "amount": 1000.00,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "description": "Test Payment",
    "beneficiaryName": "ABC Corp",
    "beneficiaryAccount": "1234567890",
    "beneficiaryBank": "XYZ Bank"
  }'
```

**Execute Payment (use the order ID from above response):**
```bash
curl -X POST http://localhost:8080/api/v1/payment-executions/execute/1
```

**Check Order Status:**
```bash
curl http://localhost:8080/api/v1/payment-orders/1
```

### Step 5: View Database (H2 Console)
Navigate to: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:paymentdb`
- Username: `sa`
- Password: (leave blank)

## 📊 Sample Data Flow

1. **Create Order** → Order ID: 1, Reference: ORD-XXXXX, Status: PENDING
2. **Execute Payment** → Execution ID: 1, Status: PROCESSING → SUCCESS
3. **Order Updated** → Status: COMPLETED

## 🎯 Common Operations

### Get All Orders
```bash
curl http://localhost:8080/api/v1/payment-orders
```

### Get Orders by Status
```bash
curl http://localhost:8080/api/v1/payment-orders/status/COMPLETED
```

### Get Orders for a Customer
```bash
curl http://localhost:8080/api/v1/payment-orders/customer/CUST001
```

### Get Execution History
```bash
curl http://localhost:8080/api/v1/payment-executions/order/1
```

### Retry Failed Payment
```bash
curl -X POST http://localhost:8080/api/v1/payment-executions/1/retry
```

## 📁 Project Structure Overview

```
payment-system/
├── src/main/java/com/example/paymentsystem/
│   ├── controller/          # REST API endpoints
│   ├── service/            # Business logic
│   ├── repository/         # Data access
│   ├── entity/             # Database models
│   ├── dto/                # API request/response
│   ├── config/             # Configuration
│   └── exception/          # Error handling
├── src/main/resources/
│   └── application.yml     # Configuration
├── src/test/               # Unit tests
├── pom.xml                 # Dependencies
├── README.md               # Full documentation
├── API_COLLECTION.md       # API examples
└── ARCHITECTURE.md         # Architecture docs
```

## 🔍 Key Features to Explore

✅ **Payment Order Management** - Create, update, query orders  
✅ **Payment Execution** - Execute, retry, settle payments  
✅ **Status Tracking** - Real-time status updates  
✅ **Error Handling** - Comprehensive error management  
✅ **API Documentation** - Interactive Swagger UI  
✅ **Database Console** - H2 web interface  

## 🛠️ IDE Setup (IntelliJ IDEA)

1. Open IntelliJ IDEA
2. File → Open → Select `payment-system` folder
3. Wait for Maven import to complete
4. Right-click `PaymentSystemApplication.java` → Run

## 📚 Next Steps

1. Read [README.md](README.md) for detailed documentation
2. Check [API_COLLECTION.md](API_COLLECTION.md) for all endpoints
3. Study [ARCHITECTURE.md](ARCHITECTURE.md) for system design
4. Run tests: `mvn test`
5. Explore Swagger UI for interactive API testing

## ❓ Troubleshooting

**Port 8080 already in use?**
```yaml
# Edit application.yml
server:
  port: 8081  # Change to different port
```

**Build fails?**
```bash
mvn clean install -U  # Force update dependencies
```

**Cannot connect to H2?**
- Check application is running
- Verify URL: jdbc:h2:mem:paymentdb
- Username: sa, Password: (blank)

## 🎉 You're Ready!

The payment system is now running and ready to process payments. Start with the Swagger UI to explore all available APIs.

Happy coding! 🚀
