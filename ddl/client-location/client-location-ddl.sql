CREATE TABLE IF NOT EXISTS locations."ClientLocation"
(
    id bigint NOT NULL,
    coordinates "varchar",
    nearest_city "varchar",
    ts "timestamp"
);

ALTER TABLE locations."ClientLocation"
    OWNER to test;

ALTER TABLE locations."ClientLocation"
    REPLICA IDENTITY FULL;