CREATE TABLE IF NOT EXISTS accounts."ClientTransaction"
(
    id bigint NOT NULL,
    account_id "varchar",
    amount "numeric",
    ts "timestamp"
);

ALTER TABLE accounts."ClientTransaction"
    OWNER to test;

ALTER TABLE accounts."ClientTransaction"
    REPLICA IDENTITY FULL;