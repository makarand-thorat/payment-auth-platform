CREATE TABLE accounts (
    id                BIGSERIAL PRIMARY KEY,
    card_number       VARCHAR(16) UNIQUE NOT NULL,
    holder_name       VARCHAR(100) NOT NULL,
    balance_in_cents  BIGINT NOT NULL DEFAULT 0,
    daily_limit_cents BIGINT NOT NULL DEFAULT 100000,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO accounts (card_number, holder_name, balance_in_cents, daily_limit_cents, status) VALUES
('1234567890123456', 'Alice Johnson',  500000, 200000, 'ACTIVE'),
('2345678901234567', 'Bob Smith',      250000, 100000, 'ACTIVE'),
('3456789012345678', 'Carol White',    750000, 300000, 'ACTIVE'),
('4567890123456789', 'David Brown',    100000, 100000, 'ACTIVE'),
('5678901234567890', 'Eve Davis',      50000,  100000, 'BLOCKED');