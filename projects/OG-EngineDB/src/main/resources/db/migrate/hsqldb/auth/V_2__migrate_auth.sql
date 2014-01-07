START TRANSACTION;

  CREATE TABLE auth_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO auth_schema_version (version_key, version_value) VALUES ('schema_patch', '2');

  -- HSQL does not allow a sequence to be started or reset to a value from a variable; the grammar only accepts an integer literal.
  -- Fortunately it's rare that HSQL databases will need to be upgraded, so we start with a large number that's likely
  -- to be greater than the current sequence value. 
  CREATE SEQUENCE auth_hibernate_sequence AS bigint
      START WITH 1000000 INCREMENT BY 1;

  DROP SEQUENCE hibernate_sequence IF EXISTS;

COMMIT;
