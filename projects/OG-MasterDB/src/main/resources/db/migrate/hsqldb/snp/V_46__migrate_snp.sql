START TRANSACTION;
  UPDATE snp_schema_version SET version_value='46' WHERE version_key='schema_patch';

  ALTER TABLE snp_snapshot
    ADD COLUMN snapshot_type varchar(255) NULL;

  -- Default all existing values to the structured snapshot
  UPDATE snp_snapshot
  SET snapshot_type = 'com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot';

  ALTER TABLE snp_snapshot ALTER COLUMN snapshot_type SET NOT NULL;

  CREATE INDEX ix_snp_snapshot_name_type ON snp_snapshot(name, snapshot_type);

COMMIT;
