# Payment Authorization Platform

A real-time payment authorization platform built with Java, Spring Boot, Kafka, and PostgreSQL.
Simulates card transaction processing with fraud detection, rules engine, and distributed microservices architecture.

## Architecture
Client
│
▼
API Gateway (port 8080)
│
├──► Account Service (port 8081) ──► PostgreSQL
│
└──► Kafka (transaction.submitted topic)
│
▼
Authorization Service (port 8082)
│
▼
Kafka (transaction.result topic)

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Entry point — validates requests, checks account, debits balance, publishes Kafka events |
| account-service | 8081 | Manages account data, balances, and spending limits |
| authorization-service | 8082 | Consumes transaction events, makes authorization decisions |

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Messaging | Apache Kafka |
| Database | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| DB Migrations | Flyway |
| Containerization | Docker |
| Build Tool | Maven |

## Prerequisites

- Java 21+
- Docker Desktop
- Maven 3.8+
- Postman (for testing)

## Running Locally

### Step 1 — Start infrastructure

```bash
docker compose up -d
```

This starts:
- PostgreSQL on port 5432
- Zookeeper on port 2181
- Kafka on port 9092

### Step 2 — Start services in order

1. `account-service` — port 8081
2. `api-gateway` — port 8080
3. `authorization-service` — port 8082

Run each via Eclipse: right click → Run As → Spring Boot App

## API Reference

### Authorize a transaction
POST http://localhost:8080/authorize
Content-Type: application/json
{
"cardNumber": "1234567890123456",
"amountInCents": 5000,
"merchantId": "MERCHANT_001",
"merchantCategory": "RETAIL",
"timestamp": "2026-04-21T10:30:00"
}

### Response — Approved

```json
{
  "transactionId": "uuid",
  "cardNumber": "1234567890123456",
  "amountInCents": 5000,
  "status": "APPROVED",
  "message": "Transaction approved"
}
```

### Response — Declined

```json
{
  "transactionId": "uuid",
  "cardNumber": "1234567890123456",
  "amountInCents": 5000,
  "status": "DECLINED",
  "message": "Insufficient funds"
}
```

### Get account details
GET http://localhost:8081/accounts/{cardNumber}

### Health checks
GET http://localhost:8080/health
GET http://localhost:8081/health
GET http://localhost:8082/health

## Test Accounts

| Card Number | Holder | Balance | Status |
|---|---|---|---|
| 1234567890123456 | Alice Johnson | €5,000.00 | ACTIVE |
| 2345678901234567 | Bob Smith | €2,500.00 | ACTIVE |
| 3456789012345678 | Carol White | €7,500.00 | ACTIVE |
| 4567890123456789 | David Brown | €1,000.00 | ACTIVE |
| 5678901234567890 | Eve Davis | €500.00 | BLOCKED |

## Transaction Flow

Client sends POST /authorize to API Gateway
Gateway validates the request
Gateway calls Account Service to verify account exists and is active
Gateway checks sufficient balance
If approved — Gateway debits the account
Gateway publishes TransactionEvent to Kafka (transaction.submitted topic)
Authorization Service consumes the event
Authorization Service publishes AuthorizationResult to Kafka (transaction.result topic)


## Project Structure
payment-auth-platform/
├── api-gateway/                  # Spring Boot — entry point
│   └── src/main/java/com/payment/gateway/
│       ├── controller/           # REST endpoints
│       ├── service/              # Business logic
│       ├── client/               # Account Service HTTP client
│       ├── kafka/                # Kafka producer
│       ├── dto/                  # Data transfer objects
│       └── config/               # App configuration
│
├── account-service/              # Spring Boot — account management
│   └── src/main/java/com/payment/account/
│       ├── controller/           # REST endpoints
│       ├── service/              # Business logic
│       ├── repository/           # JPA repositories
│       ├── model/                # JPA entities
│       ├── dto/                  # Data transfer objects
│       └── exception/            # Global exception handler
│
├── authorization-service/        # Spring Boot — authorization decisions
│   └── src/main/java/com/payment/authorization/
│       ├── kafka/                # Kafka consumer and producer
│       ├── service/              # Authorization logic
│       ├── dto/                  # Data transfer objects
│       └── config/               # Kafka configuration
│
├── docs/                         # Architecture Decision Records
│   └── ADR-001-why-kafka.md
│
└── docker-compose.yml            # Infrastructure setup

## Architecture Decisions

See the [docs/](docs/) folder for Architecture Decision Records (ADRs) explaining key design choices.
## Upcoming Features (Days 8–30)

- Rules Engine with configurable business rules
- Fraud Detection Service with Redis velocity tracking
- gRPC communication between services
- Cassandra audit log
- Kubernetes deployment
- GitHub Actions CI/CD pipeline
- Gatling load testing
- Prometheus and Grafana monitoring
- Distributed tracing with Jaeger