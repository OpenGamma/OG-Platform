START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='56' WHERE version_key='schema_patch';

  -- HSQL does not allow a sequence to be started or reset to a value from a variable; the grammar only accepts an integer literal.
  -- Fortunately it's rare that HSQL databases will need to be upgraded, so we start with a large number that's likely
  -- to be greater than the current sequence value. 
  CREATE SEQUENCE sec_hibernate_sequence AS bigint
      START WITH 1000000 INCREMENT BY 1;
      
COMMIT;
