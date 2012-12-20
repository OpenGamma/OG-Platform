START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='56' WHERE version_key='schema_patch';

  CREATE SEQUENCE sec_hibernate_sequence START WITH 1 INCREMENT BY 1;
  
  CREATE FUNCTION copy_sequence_value (from_name text, to_name text)
    RETURNS bigint AS $$
    BEGIN
      IF ((SELECT COUNT(c.relname) FROM pg_class c WHERE c.relkind = 'S' AND c.relname = from_name) > 0) THEN
        RETURN setval(to_name, nextval(from_name));
      END IF;
      RETURN 1;
    END
  $$ LANGUAGE plpgsql;
  SELECT copy_sequence_value('hibernate_sequence', 'sec_hibernate_sequence');
  DROP FUNCTION copy_sequence_value (from_name text, to_name text);
	  
COMMIT;
