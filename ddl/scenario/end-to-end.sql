--Accounts Schema
CREATE SCHEMA accounts
    AUTHORIZATION test;

--Clients table
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

--Location table
CREATE TABLE IF NOT EXISTS accounts."ClientLocation"
(
    id bigint NOT NULL,
    coordinates "varchar",
    nearest_city "varchar",
    ts bigint
);

ALTER TABLE accounts."ClientLocation"
    OWNER to test;

ALTER TABLE accounts."ClientLocation"
    REPLICA IDENTITY FULL;

--Transaction table
CREATE TABLE IF NOT EXISTS accounts."ClientTransaction"
(
    id bigint NOT NULL,
    account_id "varchar",
    amount "numeric",
    ts bigint
);

ALTER TABLE accounts."ClientTransaction"
    OWNER to test;

ALTER TABLE accounts."ClientTransaction"
    REPLICA IDENTITY FULL;

--Insert Clients
INSERT INTO accounts."Clients" VALUES
    (1, 'Alex', 'Sergeenko', 'male', 'Lenina st. 28, 218'),
    (2, 'John', 'Doe', 'male', 'Nekrasova st. 21, 19'),
    (3, 'Sam', 'Smith', 'male', 'Fleet st. 19, 62'),
    (4, 'Mary', 'Carpenter', 'female', 'Broadway 72, 378'),
    (5, 'Ashley', 'May', 'female', 'Manhattan dr. 321, 129')

--Insert Locations
INSERT INTO accounts."ClientLocation" VALUES
    (1, '40.689015, -74.045110', 'New York', 1621744725),
    (5, '19.903092, -75.097198', 'Guantánamo', 1625244725)

--Insert Transactions
INSERT INTO accounts."ClientTransaction" VALUES
    (1, '445536', 300, 1621744025),
    (5, '144553', 1200, 1621714725),
    (8, '449536', 500, 1621744425)