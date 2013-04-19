START TRANSACTION;
  UPDATE cfg_schema_version SET version_value='47' WHERE version_key='schema_patch';
  
  ALTER TABLE cfg_config DROP CONSTRAINT name_type_unique;
  ALTER TABLE cfg_config DROP CONSTRAINT oid_unique;
COMMIT;