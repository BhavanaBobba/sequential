

CREATE SEQUENCE sequential.sequential_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999
  START 1
  CACHE 1
  CYCLE;
ALTER TABLE sequential.sequential_seq OWNER TO postgres;
alter table sequential.consumption_type alter column consumption_type_id set default nextval('sequential.sequential_seq');

ALTER TABLE sequential.consumption_type ADD CONSTRAINT consumption_type_ukey UNIQUE (consumption_type);

ALTER TABLE sequential.consumption_assignment ADD CONSTRAINT consumption_assignment_type_id_fkey
FOREIGN KEY (consumption_type_id) REFERENCES sequential.consumption_type (consumption_type_id)
MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

alter table sequential.consumption_rule alter column consumption_rule_id set default nextval('sequential.sequential_seq');

