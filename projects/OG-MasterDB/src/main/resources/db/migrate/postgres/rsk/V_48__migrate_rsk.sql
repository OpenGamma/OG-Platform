START TRANSACTION;

  -- update the version
  UPDATE rsk_schema_version SET version_value='48' WHERE version_key='schema_patch';

  ALTER TABLE rsk_computation_target ALTER COLUMN id_scheme DROP NOT NULL;
  ALTER TABLE rsk_computation_target ALTER COLUMN id_value DROP NOT NULL;
  ALTER TABLE rsk_computation_target ALTER COLUMN id_version DROP NOT NULL;
  
COMMIT;
