START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='56' WHERE version_key='schema_patch';

  CREATE SEQUENCE sec_hibernate_sequence START WITH 1 INCREMENT BY 1;
  DO $$BEGIN
    IF ((SELECT COUNT(c.relname) FROM pg_class c WHERE c.relkind = 'S' AND c.relname = 'hibernate_sequence') = 1) THEN
      PERFORM setval('sec_hibernate_sequence', nextval('hibernate_sequence'));
    END IF;
  END$$;
	  
COMMIT;
