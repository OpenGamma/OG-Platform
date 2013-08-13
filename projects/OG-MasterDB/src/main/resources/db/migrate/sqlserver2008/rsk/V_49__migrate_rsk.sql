BEGIN TRAN;

  UPDATE rsk_schema_version SET version_value='49' WHERE version_key='schema_patch';

  ALTER TABLE rsk_value DROP CONSTRAINT rsk_chk_uq_value;
  ALTER TABLE rsk_value ADD CONSTRAINT rsk_chk_uq_value
      UNIQUE (run_id, calculation_configuration_id, name, value_specification_id, computation_target_id);

  ALTER TABLE rsk_run_status ADD run_id BIGINT DEFAULT 0 NOT NULL;
  ALTER TABLE rsk_run_status DROP CONSTRAINT rsk_chk_uq_run_status;
  ALTER TABLE rsk_run_status ADD CONSTRAINT rsk_chk_uq_run_status
      UNIQUE (run_id, calculation_configuration_id, computation_target_id);
  
COMMIT;
