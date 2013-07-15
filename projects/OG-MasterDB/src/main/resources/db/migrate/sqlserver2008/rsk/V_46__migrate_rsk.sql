BEGIN TRAN;

  -- update the version
  UPDATE rsk_schema_version SET version_value='46' WHERE version_key='schema_patch';
    
  CREATE TABLE rsk_hibernate_sequence (
    next_val numeric(19,0) 
  );
  IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_NAME='hibernate_sequence') 
    INSERT INTO rsk_hibernate_sequence (next_val) SELECT next_val FROM hibernate_sequence;
  
COMMIT;
