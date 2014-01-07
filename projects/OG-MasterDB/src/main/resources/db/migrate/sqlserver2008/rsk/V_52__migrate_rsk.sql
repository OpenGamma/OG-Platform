BEGIN TRAN;

  UPDATE rsk_schema_version SET version_value='52' WHERE version_key='schema_patch';

  ALTER TABLE rsk_run ADD cycle_name VARCHAR(255);

COMMIT;
