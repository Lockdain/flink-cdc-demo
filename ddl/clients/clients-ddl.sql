CREATE TABLE IF NOT EXISTS accounts."Clients"
(
    id bigint NOT NULL,
    name "char(60)",
    surname "char(60)",
    gender "char(60)",
    address "char(60)",
    PRIMARY KEY (id)
);

ALTER TABLE accounts."Clients"
    OWNER to test;

ALTER TABLE accounts."Clients"
    REPLICA IDENTITY FULL;