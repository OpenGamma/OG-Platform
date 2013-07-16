START TRANSACTION;

  UPDATE rsk_schema_version SET version_value='48' WHERE version_key='schema_patch';

  ALTER TABLE rsk_computation_target ALTER COLUMN id_scheme set null;
  ALTER TABLE rsk_computation_target ALTER COLUMN id_value set null;
  
COMMIT;

