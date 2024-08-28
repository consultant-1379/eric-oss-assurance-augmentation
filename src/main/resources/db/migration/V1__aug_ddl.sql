--
-- COPYRIGHT Ericsson 2023
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

SET search_path TO "${augmentSchemaName}";

GRANT USAGE ON SCHEMA "${augmentSchemaName}" TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS registration (
  ardq_id text CONSTRAINT pk_reg PRIMARY KEY,
  ardq_url text NOT NULL,
  rules json NOT NULL
);

ALTER TABLE IF EXISTS registration
  OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS schema (
  reference text CONSTRAINT pk_schema PRIMARY KEY,
  name text NOT NULL,
  schema json
);

ALTER TABLE IF EXISTS schema
  OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS io_schema (
  ardq_id text,
  input_schema_ref text,
  output_schema_ref text,
  PRIMARY KEY (ardq_id, input_schema_ref, output_schema_ref),
  CONSTRAINT fk_reg_io_schema
    FOREIGN KEY (ardq_id)
    REFERENCES registration (ardq_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_schema_in_schema
    FOREIGN KEY (input_schema_ref)
    REFERENCES schema (reference)
    ON DELETE CASCADE,
  CONSTRAINT fk_schema_out_schema
    FOREIGN KEY (output_schema_ref)
    REFERENCES schema (reference)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS io_schema
  OWNER TO "${databaseUser}";

CREATE TABLE IF NOT EXISTS field (
  ardq_id text,
  input_schema_ref text,
  augment_field text NOT NULL,
  field_spec json NOT NULL,
  PRIMARY KEY (ardq_id, input_schema_ref, augment_field),
  CONSTRAINT fk_reg_field
    FOREIGN KEY (ardq_id)
    REFERENCES registration (ardq_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_schema_field
    FOREIGN KEY (input_schema_ref)
    REFERENCES schema (reference)
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS field
  OWNER TO "${databaseUser}";
