-- Use this command to ssh on to jumbbox to connect to db locally

ssh awsjump -L 6000:awsDbPath:dbPort -- Non-Prod
ssh awsjump -L 7000:awsDbPath:dbPort -- Prod

-- Local DB details

Host      : localhost
Port      : 6000
DBName    : ****
User      : ****
Password  : ****

-- Schema to Create a Table and sequence for id column

CREATE SEQUENCE schemaName.schemaName_seq
INCREMENT 1
MINVALUE 1
MAXVALUE 999999999
START 1
CACHE 1
CYCLE;

CREATE TABLE schemaName.tableName
(
  colunm_1 numeric NOT NULL Primary key DEFAULT nextval('schemaName.schemaName_seq'::regclass),
  column_2 text NOT NULL,
  column_3 text,
  column_4 text NOT NULL
)
WITH (
OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE schemaName.tableName OWNER to <user>;
ALTER TABLE schemaName.tableName ALTER COLUMN column_1 SET DEFAULT nextval('schemaName.schemaName_seq');

