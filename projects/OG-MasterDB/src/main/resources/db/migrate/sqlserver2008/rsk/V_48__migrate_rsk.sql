BEGIN TRAN;

  -- update the version
  UPDATE rsk_schema_version SET version_value='48' WHERE version_key='schema_patch';

  ALTER TABLE rsk_computation_target ALTER COLUMN id_scheme VARCHAR(255) NULL;
  ALTER TABLE rsk_computation_target ALTER COLUMN id_value VARCHAR(255) NULL;
  ALTER TABLE rsk_computation_target ALTER COLUMN id_version VARCHAR(255) NULL;
  
COMMIT;
