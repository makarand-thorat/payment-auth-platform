# ADR 002 - Why Microservices over Monolith

## Status
Accepted

## Date
2026-04-22

## Context
The payment authorization platform needed an architectural style. 
Two main options were considered:

1. Monolith — all logic in one Spring Boot application
2. Microservices — separate services for each concern

## Decision
Use microservices with the following service boundaries:

- API Gateway — request validation and routing
- Account Service — account data and balance management
- Rules Engine — business rule evaluation
- Fraud Service — fraud signal detection and scoring
- Authorization Service — decision aggregation

## Reasons

**Independent scaling** — Fraud detection is computationally 
intensive. With microservices, only the Fraud Service needs 
more resources during high load — not the entire platform.

**Independent deployment** — A bug fix in the Rules Engine 
can be deployed without touching the Account Service or 
Gateway. With a monolith, every change requires a full 
redeploy.

**Team ownership** — Each service has a clear owner. In a 
real organization, different teams own different services.

**Technology flexibility** — The Fraud Service uses Redis. 
The Authorization Service uses Cassandra. Each service picks 
the right tool for its job.

## Consequences

- Added operational complexity — 5 services to deploy and monitor
- Network latency between services
- Distributed transactions are harder than local transactions
- Requires Kafka for reliable async communication
- More infrastructure (Kafka, Redis, Cassandra) vs a simple DB

## Mitigation
Kafka absorbs the latency between services. Each service is 
independently deployable via Docker and Kubernetes.