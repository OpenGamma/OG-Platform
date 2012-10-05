START TRANSACTION;
  UPDATE cfg_schema_version SET version_value='46' WHERE version_key='schema_patch';
  
  ALTER TABLE cfg_config DROP CONSTRAINT name_type_unique;
  ALTER  TABLE cfg_config ADD CONSTRAINT name_type_unique UNIQUE (name, config_type, corr_from_instant, ver_from_instant); 
  ALTER  TABLE cfg_config ADD CONSTRAINT oid_unique UNIQUE (oid, corr_from_instant, ver_from_instant);
COMMIT;