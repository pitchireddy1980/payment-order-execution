# Payment System Architecture Documentation

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  (Mobile Apps, Web Browsers, API Clients, Third-party Systems)  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway / Load Balancer                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              Controller Layer                          │    │
│  │  - PaymentOrderController                             │    │
│  │  - PaymentExecutionController                         │    │
│  │  - GlobalExceptionHandler                             │    │
│  └───────────────────────┬────────────────────────────────┘    │
│                          │                                       │
│  ┌───────────────────────▼────────────────────────────────┐    │
│  │              Service Layer                             │    │
│  │  - PaymentOrderService                                │    │
│  │  - PaymentExecutionService                            │    │
│  │  - PaymentGatewayService                              │    │
│  └───────────────────────┬────────────────────────────────┘    │
│                          │                                       │
│  ┌───────────────────────▼────────────────────────────────┐    │
│  │            Repository Layer                            │    │
│  │  - PaymentOrderRepository                             │    │
│  │  - PaymentExecutionRepository                         │    │
│  └───────────────────────┬────────────────────────────────┘    │
│                          │                                       │
└──────────────────────────┼───────────────────────────────────────┘
                           │
                           │ JPA/Hibernate
                           ▼
                  ┌─────────────────┐
                  │    Database     │
                  │  (H2/PostgreSQL) │
                  └─────────────────┘
```

## Layered Architecture

### 1. Controller Layer (Presentation)
**Responsibility**: Handle HTTP requests and responses

**Components**:
- `PaymentOrderController`: Manages payment order endpoints
- `PaymentExecutionController`: Manages payment execution endpoints
- `GlobalExceptionHandler`: Centralized exception handling

**Key Features**:
- Request validation using Bean Validation
- DTO transformation
- HTTP status code management
- API documentation with Swagger annotations

### 2. Service Layer (Business Logic)
**Responsibility**: Implement business rules and orchestrate operations

**Components**:
- `PaymentOrderService`: Order lifecycle management
- `PaymentExecutionService`: Payment execution logic
- `PaymentGatewayService`: Gateway integration

**Key Features**:
- Transaction management (@Transactional)
- Business validation
- State management
- Error handling and retry logic

### 3. Repository Layer (Data Access)
**Responsibility**: Database operations and queries

**Components**:
- `PaymentOrderRepository`: Order data access
- `PaymentExecutionRepository`: Execution data access

**Key Features**:
- JPA repository pattern
- Custom query methods
- JPQL for complex queries
- Query optimization

### 4. Entity Layer (Domain Model)
**Responsibility**: Represent business entities

**Components**:
- `PaymentOrder`: Order entity
- `PaymentExecution`: Execution entity
- Enums: Status types, payment methods

**Key Features**:
- JPA annotations
- Entity relationships
- Audit timestamps
- Data validation

## Data Flow Diagram

### Create and Execute Payment Flow

```
Client
  │
  │ 1. POST /v1/payment-orders
  ▼
PaymentOrderController
  │
  │ 2. Validate DTO
  ▼
PaymentOrderService
  │
  │ 3. Create Order Entity
  │ 4. Set PENDING status
  ▼
PaymentOrderRepository
  │
  │ 5. Save to Database
  ▼
Database
  │
  │ 6. Return saved order
  ◄───────────────────┐
                      │
Client                │
  │                   │
  │ 7. POST /v1/payment-executions/execute/{orderId}
  ▼                   │
PaymentExecutionController
  │                   │
  │ 8. Execute payment
  ▼                   │
PaymentExecutionService
  │                   │
  │ 9. Create execution record
  │ 10. Update order to PROCESSING
  ▼                   │
PaymentGatewayService
  │                   │
  │ 11. Process payment
  │ 12. Set execution status
  ▼                   │
PaymentExecutionRepository
  │                   │
  │ 13. Save execution
  ▼                   │
Database              │
  │                   │
  │ 14. Return execution
  └───────────────────┘
```

## Entity Relationship Diagram

```
┌─────────────────────────┐
│    PaymentOrder         │
│─────────────────────────│
│ id (PK)                 │
│ orderReference (UNIQUE) │
│ customerId              │
│ customerName            │
│ customerEmail           │
│ amount                  │
│ currency                │
│ status                  │
│ paymentMethod           │
│ beneficiaryName         │
│ beneficiaryAccount      │
│ beneficiaryBank         │
│ createdAt               │
│ updatedAt               │
│ scheduledAt             │
│ completedAt             │
└────────┬────────────────┘
         │
         │ 1:N
         │
         ▼
┌─────────────────────────────┐
│   PaymentExecution          │
│─────────────────────────────│
│ id (PK)                     │
│ executionReference (UNIQUE) │
│ paymentOrderId (FK)         │
│ status                      │
│ amount                      │
│ currency                    │
│ gatewayTransactionId        │
│ gatewayProvider             │
│ retryAttempt                │
│ errorMessage                │
│ errorCode                   │
│ createdAt                   │
│ updatedAt                   │
│ processedAt                 │
│ settledAt                   │
└─────────────────────────────┘
```

## State Transition Diagrams

### Payment Order States

```
        ┌─────────┐
   ┌───▶│ PENDING │◄────┐
   │    └────┬────┘     │
   │         │          │
   │         ▼          │
   │    ┌──────────┐    │
   │    │SCHEDULED │    │
   │    └────┬─────┘    │
   │         │          │
   │         ▼          │
   │    ┌───────────┐   │
   │    │PROCESSING │   │
   │    └─────┬─────┘   │
   │          │         │
   │     ┌────┴────┐    │
   │     ▼         ▼    │
   │ ┌────────┐ ┌──────┐
   │ │ FAILED │ │COMPL-│
   │ └────────┘ │ETED  │
   │            └──┬───┘
   │               │
   │               ▼
   │          ┌─────────┐
   │          │REFUNDED │
   │          └─────────┘
   │
   │    ┌──────────┐
   └────│CANCELLED │
        └──────────┘
```

### Payment Execution States

```
┌──────────┐
│INITIATED │
└────┬─────┘
     │
     ▼
┌──────────┐
│ PENDING  │
└────┬─────┘
     │
     ▼
┌───────────┐
│PROCESSING │
└─────┬─────┘
      │
  ┌───┴───┐
  ▼       ▼
┌──────┐ ┌────────┐
│FAILED│ │SUCCESS │
└──────┘ └───┬────┘
             │
             ▼
        ┌─────────┐
        │ SETTLED │
        └────┬────┘
             │
             ▼
        ┌──────────┐
        │ REVERSED │
        └──────────┘
```

## Component Interaction Sequence

### Successful Payment Flow

```
Client    Controller    Service    Gateway    Repository    Database
  │           │            │          │            │            │
  ├──POST────▶│            │          │            │            │
  │           ├──create───▶│          │            │            │
  │           │            ├─validate─┤            │            │
  │           │            │          │            │            │
  │           │            ├─────────save─────────▶│            │
  │           │            │          │            ├───INSERT──▶│
  │           │            │          │            │◄──────────┤
  │           │            │◄─────────┼────────────┤            │
  │           │◄──────────┤          │            │            │
  │◄──200────┤            │          │            │            │
  │           │            │          │            │            │
  ├─POST execute──────────▶│          │            │            │
  │           │            ├──create execution─────▶│            │
  │           │            │          │            ├───INSERT──▶│
  │           │            ├─process─▶│            │            │
  │           │            │          ├─API call─┐ │            │
  │           │            │          │◄─────────┘ │            │
  │           │            │◄────────┤            │            │
  │           │            ├────update status──────▶│            │
  │           │            │          │            ├───UPDATE──▶│
  │           │◄──────────┤          │            │            │
  │◄──200────┤            │          │            │            │
```

## Security Architecture

```
┌──────────────────────────────────────────────────┐
│              Security Layer                      │
│  - Authentication (JWT/OAuth)                   │
│  - Authorization (Role-based)                   │
│  - Rate Limiting                                │
│  - Input Validation                             │
└────────────────┬─────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────┐
│           Application Layer                      │
│  - Business Logic                               │
│  - Data Processing                              │
└────────────────┬─────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────┐
│            Data Layer                            │
│  - Encrypted Storage                            │
│  - Audit Logging                                │
└──────────────────────────────────────────────────┘
```

## Deployment Architecture

```
                ┌──────────────────┐
                │   Load Balancer  │
                └────────┬─────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
  ┌──────────┐    ┌──────────┐    ┌──────────┐
  │  App     │    │  App     │    │  App     │
  │Instance 1│    │Instance 2│    │Instance 3│
  └────┬─────┘    └────┬─────┘    └────┬─────┘
       │               │               │
       └───────────────┼───────────────┘
                       │
                       ▼
              ┌────────────────┐
              │   Database     │
              │   (Primary)    │
              └────────┬───────┘
                       │
                       │ Replication
                       ▼
              ┌────────────────┐
              │   Database     │
              │  (Read Replica)│
              └────────────────┘
```

## Design Patterns Used

1. **Layered Architecture**: Clear separation of concerns
2. **Repository Pattern**: Data access abstraction
3. **Service Layer Pattern**: Business logic encapsulation
4. **DTO Pattern**: Data transfer between layers
5. **Dependency Injection**: Loose coupling via Spring DI
6. **Factory Pattern**: Entity/DTO creation
7. **Strategy Pattern**: Different payment methods
8. **State Pattern**: Order and execution status management

## Key Design Decisions

### 1. Separation of Concerns
- Each layer has a single, well-defined responsibility
- Changes in one layer don't affect others

### 2. Stateless Services
- Services don't maintain state between requests
- Enables horizontal scaling

### 3. Database Transaction Management
- @Transactional at service layer
- ACID compliance for critical operations

### 4. Exception Handling
- Global exception handler
- Consistent error responses
- Proper HTTP status codes

### 5. API Versioning
- URL-based versioning (/v1/)
- Future-proof for breaking changes

## Performance Considerations

1. **Database Indexing**: 
   - Index on orderReference, executionReference
   - Index on customerId, status
   - Composite indexes for common queries

2. **Lazy Loading**: 
   - OneToMany relationships use lazy fetching
   - Prevents N+1 query problems

3. **Connection Pooling**: 
   - HikariCP for efficient connection management

4. **Caching Strategy** (Future):
   - Redis for frequently accessed data
   - Cache invalidation on updates

## Scalability Strategy

1. **Horizontal Scaling**: 
   - Stateless design allows multiple instances

2. **Database Sharding**: 
   - Partition by customerId for large datasets

3. **Async Processing**: 
   - Queue-based execution for high volume

4. **Read Replicas**: 
   - Separate read and write operations
