BEGIN TRAN;

  UPDATE cfg_schema_version SET version_value='46' WHERE version_key='schema_patch';
  
  ALTER TABLE cfg_config DROP CONSTRAINT name_type_unique;
  
COMMIT;
