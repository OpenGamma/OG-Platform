START TRANSACTION;

  -- update the version
  UPDATE rsk_schema_version SET version_value='47' WHERE version_key='schema_patch';

  ALTER TABLE rsk_live_data_snapshot_entry DROP CONSTRAINT rsk_chk_uq_snapshot_entry;
	  
COMMIT;
