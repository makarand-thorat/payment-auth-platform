# ADR 001 - Why Kafka for inter-service communication

## Status
Accepted

## Date
2026-04-20

## Context
The payment authorization platform needs multiple services to react to 
a single transaction event. Options considered:

1. REST calls from Gateway to each service synchronously
2. Kafka topics for async event-driven communication

## Decision
Use Kafka for communication between API Gateway and downstream services.

## Reasons

**Decoupling** - Services don't need to know each other's URLs. The Gateway 
publishes an event and doesn't care who consumes it.

**Resilience** - If the Authorization Service goes down, messages queue up 
in Kafka and are processed when it recovers. With REST, the request is lost.

**Scalability** - Multiple consumers can read from the same topic 
independently. Adding a new service (Rules Engine, Fraud Service) requires 
zero changes to the Gateway.

**Replay** - Kafka retains messages. If a bug is found, events can be 
replayed after the fix is deployed.

## Consequences
- Added operational complexity (Kafka requires Zookeeper)
- Transactions are processed asynchronously - Gateway returns before 
  Authorization Service makes its decision
- Requires careful handling of message serialization/deserialization