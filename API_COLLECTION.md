# Payment System API Collection

## Base URL
```
http://localhost:8080/api
```

## Payment Orders API

### 1. Create Payment Order
```http
POST /v1/payment-orders
Content-Type: application/json

{
  "customerId": "CUST001",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "amount": 1000.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "description": "Payment for Order #12345",
  "beneficiaryName": "ABC Corporation",
  "beneficiaryAccount": "1234567890",
  "beneficiaryBank": "XYZ Bank",
  "beneficiaryBankCode": "XYZ001"
}
```

### 2. Get Order by ID
```http
GET /v1/payment-orders/1
```

### 3. Get Order by Reference
```http
GET /v1/payment-orders/reference/ORD-ABC12345
```

### 4. Get All Orders
```http
GET /v1/payment-orders
```

### 5. Get Orders by Customer ID
```http
GET /v1/payment-orders/customer/CUST001
```

### 6. Get Orders by Status
```http
GET /v1/payment-orders/status/PENDING
```

Available statuses: PENDING, SCHEDULED, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED

### 7. Get Orders by Date Range
```http
GET /v1/payment-orders/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
```

### 8. Get Orders by Amount Range
```http
GET /v1/payment-orders/amount-range?minAmount=100&maxAmount=5000
```

### 9. Update Order
```http
PUT /v1/payment-orders/1
Content-Type: application/json

{
  "customerName": "John Updated Doe",
  "customerEmail": "john.updated@example.com",
  "description": "Updated payment description"
}
```

### 10. Update Order Status
```http
PATCH /v1/payment-orders/1/status?status=PROCESSING
```

### 11. Cancel Order
```http
PATCH /v1/payment-orders/1/cancel
```

### 12. Delete Order
```http
DELETE /v1/payment-orders/1
```

### 13. Get Customer Statistics
```http
GET /v1/payment-orders/customer/CUST001/stats?status=COMPLETED
```

## Payment Executions API

### 1. Execute Payment
```http
POST /v1/payment-executions/execute/1
```

### 2. Get Execution by ID
```http
GET /v1/payment-executions/1
```

### 3. Get Execution by Reference
```http
GET /v1/payment-executions/reference/EXE-ABC12345
```

### 4. Get Executions by Order ID
```http
GET /v1/payment-executions/order/1
```

### 5. Get Executions by Order Reference
```http
GET /v1/payment-executions/order-reference/ORD-ABC12345
```

### 6. Get Executions by Status
```http
GET /v1/payment-executions/status/SUCCESS
```

Available statuses: INITIATED, PENDING, PROCESSING, SUCCESS, FAILED, TIMEOUT, SETTLED, REVERSED

### 7. Get Executions by Customer ID
```http
GET /v1/payment-executions/customer/CUST001
```

### 8. Update Execution Status
```http
PATCH /v1/payment-executions/1/status?status=SUCCESS
```

### 9. Retry Failed Execution
```http
POST /v1/payment-executions/1/retry
```

### 10. Process Settlement
```http
POST /v1/payment-executions/1/settle
```

### 11. Reverse Execution
```http
POST /v1/payment-executions/1/reverse
```

## Sample Response Format

All responses follow this structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-02-13T10:30:00"
}
```

## Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-02-13T10:30:00"
}
```

## Complete Workflow Example

### Step 1: Create a Payment Order
```http
POST /v1/payment-orders
{
  "customerId": "CUST001",
  "customerName": "Alice Smith",
  "customerEmail": "alice@example.com",
  "amount": 2500.00,
  "currency": "USD",
  "paymentMethod": "BANK_TRANSFER",
  "description": "Invoice #INV-2025-001",
  "beneficiaryName": "Tech Solutions Inc",
  "beneficiaryAccount": "9876543210",
  "beneficiaryBank": "Global Bank",
  "beneficiaryBankCode": "GB001"
}
```

Response:
```json
{
  "success": true,
  "message": "Payment order created successfully",
  "data": {
    "id": 1,
    "orderReference": "ORD-A1B2C3D4",
    "status": "PENDING",
    ...
  }
}
```

### Step 2: Execute the Payment
```http
POST /v1/payment-executions/execute/1
```

Response:
```json
{
  "success": true,
  "message": "Payment execution initiated",
  "data": {
    "id": 1,
    "executionReference": "EXE-X1Y2Z3A4",
    "status": "PROCESSING",
    "gatewayTransactionId": "GW-...",
    ...
  }
}
```

### Step 3: Check Execution Status
```http
GET /v1/payment-executions/1
```

### Step 4: If Failed, Retry
```http
POST /v1/payment-executions/1/retry
```

### Step 5: Process Settlement (if successful)
```http
POST /v1/payment-executions/1/settle
```

## Testing with cURL

### Create Order
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
    "description": "Test payment",
    "beneficiaryName": "ABC Corp",
    "beneficiaryAccount": "1234567890",
    "beneficiaryBank": "XYZ Bank"
  }'
```

### Execute Payment
```bash
curl -X POST http://localhost:8080/api/v1/payment-executions/execute/1
```

### Get Order
```bash
curl http://localhost:8080/api/v1/payment-orders/1
```

## Notes

- All timestamps are in ISO 8601 format
- Amounts use 2 decimal places
- Currency codes follow ISO 4217 (3-letter codes)
- Order references are auto-generated with format: ORD-XXXXXXXX
- Execution references are auto-generated with format: EXE-XXXXXXXX
- The payment gateway is simulated with 80% success rate for testing
