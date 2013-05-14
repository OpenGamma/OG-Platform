START TRANSACTION;

  CREATE TABLE auth_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO auth_schema_version (version_key, version_value) VALUES ('schema_patch', '2');

  CREATE SEQUENCE auth_hibernate_sequence START WITH 1 INCREMENT BY 1;
  
  CREATE FUNCTION copy_sequence_value (from_name text, to_name text)
    RETURNS bigint AS $$
    BEGIN
      IF ((SELECT COUNT(c.relname) FROM pg_class c WHERE c.relkind = 'S' AND c.relname = from_name) > 0) THEN
        RETURN setval(to_name, nextval(from_name));
      END IF;
      RETURN 1;
    END
  $$ LANGUAGE plpgsql;
  SELECT copy_sequence_value('hibernate_sequence', 'auth_hibernate_sequence');
  DROP FUNCTION copy_sequence_value (from_name text, to_name text);

  DROP SEQUENCE IF EXISTS hibernate_sequence;

COMMIT;
