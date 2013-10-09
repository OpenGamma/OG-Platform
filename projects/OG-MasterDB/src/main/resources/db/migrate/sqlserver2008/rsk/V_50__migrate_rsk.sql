BEGIN TRAN;

  UPDATE rsk_schema_version SET version_value='50' WHERE version_key='schema_patch';

  ALTER TABLE rsk_run DROP CONSTRAINT rsk_chk_uq_run;
  ALTER TABLE rsk_run ADD CONSTRAINT rsk_chk_uq_run UNIQUE (id, version_correction, viewdef_scheme, viewdef_value, viewdef_version, live_data_snapshot_id);
  ALTER TABLE rsk_failure DROP CONSTRAINT rsk_chk_uq_failure;
  ALTER TABLE rsk_failure ADD CONSTRAINT rsk_chk_uq_failure UNIQUE (run_id, calculation_configuration_id, name, value_specification_id, computation_target_id);

COMMIT;
