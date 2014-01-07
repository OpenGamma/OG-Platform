BEGIN TRAN;

  CREATE TABLE auth_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO auth_schema_version (version_key, version_value) VALUES ('schema_patch', '2');

  CREATE TABLE auth_hibernate_sequence (
    next_val numeric(19,0) 
  );
  IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='hibernate_sequence') 
    INSERT INTO auth_hibernate_sequence (next_val) SELECT next_val FROM hibernate_sequence;

  IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='hibernate_sequence') 
    DROP TABLE hibernate_sequence;

COMMIT;
