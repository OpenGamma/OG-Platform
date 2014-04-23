BEGIN TRAN;

  -- update the version
  UPDATE snp_schema_version SET version_value='46' WHERE version_key='schema_patch';

  -- Add new column and default all existing values to the structured snapshot
  ALTER TABLE snp_snapshot
    ADD COLUMN snapshot_type varchar(255) NULL;

  UPDATE snp_snapshot
  SET snapshot_type = 'com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot';

  -- Remove the default as it will not be appropriate for new values being added
  ALTER TABLE snp_snapshot
    ALTER COLUMN snapshot_type varchar(255) NOT NULL;

  CREATE INDEX ix_snp_snapshot_name_type ON snp_snapshot(name, snapshot_type);

COMMIT;
