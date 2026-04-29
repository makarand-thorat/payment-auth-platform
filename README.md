# Payment Authorization Platform

A real-time payment authorization platform built with Java, Spring Boot, Kafka, and PostgreSQL.
Simulates card transaction processing with fraud detection, rules engine, and distributed microservices architecture.

## Architecture
```
Client
  |
  v
API Gateway (port 8080)
  |
  |-----> Account Service (port 8081) -----> PostgreSQL
  |
  └-----> Kafka (transaction.submitted)
                |
                v
                Rules Engine (rules.result)---|
                Fraud Service (fraud.result)--|
                                              v
        					  Authorization Service (port 8082)
                							|
                							v
        								APPROVED → debit account → Cassandra
                                        DECLINED → no debit → Cassandra
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Entry point — validates requests, checks account, debits balance, publishes Kafka events |
| account-service | 8081 | Manages account data, balances, and spending limits |
| authorization-service | 8082 | Consumes transaction events, makes authorization decisions |
| rules-engine (Kafka consumer) | 8083 | Evaluates business rules for transactions |
| fraud-service (Kafka consumer) | 8084 | Detects potential fraud based on transaction patterns |

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Messaging | Apache Kafka |
| Database | PostgreSQL 16 |
| Audit Storage | Apache Cassandra 4.1 |
| Cache / Data Grid | Redis 7.2 |
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
- Redis on port 6379
- Cassandra on port 9042

### Step 2 — Start services in order

1. `account-service` — port 8081
2. `api-gateway` — port 8080
3. `rules-engine` — port 8083
4. `fraud-service` — port 8084
5. `authorization-service` — port 8082

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
GET http://localhost:8083/health
GET http://localhost:8084/health

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
Gateway validates the request (card number, amount, merchant)
Gateway calls Account Service to verify account exists and is active
Gateway checks sufficient balance
If account is blocked or balance insufficient — Gateway returns DECLINED immediately
Otherwise — Gateway publishes TransactionEvent to Kafka and returns PENDING
Rules Engine consumes event — evaluates daily limit, blocked categories,
amount ceiling, velocity — publishes RulesResult to rules.result topic
Fraud Service consumes event — tracks velocity in Redis, scores risk signals
— publishes FraudScore to fraud.score topic
Authorization Service collects both signals
If rules failed OR fraud score >= 70 — DECLINED, no debit
If all clear — APPROVED, Account Service debited, audit saved to Cassandra

## Business Rules

| Rule | Threshold | Configurable |
|---|---|---|
| Daily limit | Amount > account daily limit | Yes |
| Blocked category | GAMBLING, ADULT, CRYPTO | Yes |
| Amount ceiling | Single transaction > 500000 cents | Yes |
| Velocity | 3+ different merchants in 10 minutes | Yes |

## Fraud Signals

| Signal | Score Added |
|---|---|
| High velocity (5+ transactions in 60 seconds) | +40 |
| Unusual hour (midnight to 6am) | +20 |
| High amount (> 100000 cents) | +25 |
| Low remaining balance (< 10000 cents) | +15 |

Fraud score >= 70 results in DECLINED.

## Project Structure
```
payment-auth-platform/
├── api-gateway/
│   └── src/main/java/com/payment/gateway/
│       ├── controller/
│       ├── service/
│       ├── client/
│       ├── kafka/
│       ├── dto/
│       └── config/
├── account-service/
│   └── src/main/java/com/payment/account/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       └── exception/
├── authorization-service/
│   └── src/main/java/com/payment/authorization/
│       ├── aggregation/
│       ├── client/
│       ├── controller/
│       ├── kafka/
│       ├── model/
│       ├── repository/
│       ├── service/
│       ├── dto/
│       └── config/
├── rules-engine/
│   └── src/main/java/com/payment/rules/
│       ├── client/
│       ├── controller/
│       ├── kafka/
│       ├── service/
│       └── dto/
├── fraud-service/
│   └── src/main/java/com/payment/fraud/
│       ├── controller/
│       ├── kafka/
│       ├── service/
│       └── dto/
├── docs/
│   ├── ADR-001-why-kafka.md
│   ├── ADR-002-microservices-decision.md
│   └── ADR-003-cassandra-for-audit.md
└── docker-compose.yml

```

## Architecture Decisions

See the [docs/](docs/) folder for Architecture Decision Records explaining key design choices:

- ADR-001 — Why Kafka for inter-service communication
- ADR-002 — Why microservices over monolith
- ADR-003 — Why Cassandra for audit log

## Upcoming (Days 15-30)

- Docker containerization of all services
- Kubernetes deployment on GKE
- GitHub Actions CI/CD pipeline
- Gatling load testing
- Prometheus and Grafana monitoring
- Distributed tracing with Jaeger