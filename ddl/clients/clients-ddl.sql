CREATE TABLE IF NOT EXISTS accounts."Clients"
(
    id bigint NOT NULL,
    name "varchar",
    surname "varchar",
    gender "varchar",
    address "varchar",
    PRIMARY KEY (id)
);

ALTER TABLE accounts."Clients"
    OWNER to test;

ALTER TABLE accounts."Clients"
    REPLICA IDENTITY FULL;