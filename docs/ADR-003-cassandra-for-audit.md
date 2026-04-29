# ADR 003 - Why Cassandra for Audit Log

## Status
Accepted

## Date
2026-04-22

## Context
Every authorization decision needs to be stored permanently 
for compliance, dispute resolution, and analytics. Options:

1. PostgreSQL — already in use for account data
2. Cassandra — distributed NoSQL database

## Decision
Use Cassandra for the authorization audit log.

## Reasons

**Write performance** — Cassandra is optimized for high-speed 
writes. At Mastercard scale (billions of transactions per day), 
every transaction produces an audit record. Cassandra handles 
this better than a relational database.

**Time-series data** — Authorization records are append-only. 
You write once and read occasionally. Cassandra's data model 
is ideal for this pattern.

**Scalability** — Cassandra scales horizontally by adding nodes. 
PostgreSQL scales vertically (bigger machine) which has limits.

**Separation of concerns** — Keeping audit data separate from 
operational data (accounts) means a spike in audit writes 
doesn't affect account lookup performance.

## Consequences
- Two databases to manage (PostgreSQL + Cassandra)
- No joins — Cassandra is not relational
- Eventual consistency — not suitable for balance data
- Requires schema design upfront (no flexible ALTER TABLE)

## When NOT to use Cassandra
Account balances stay in PostgreSQL because they require 
strong consistency and ACID transactions. Cassandra's eventual 
consistency model is not appropriate for financial balances.