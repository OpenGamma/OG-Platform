BEGIN TRAN;
  -- update the version
  UPDATE sec_schema_version SET version_value='69' WHERE version_key='schema_patch';
  
  -- add bond future first notice date column
  ALTER TABLE sec_future ADD bondFutureFirstNoticeDate DATETIME2(6) NULL;

  -- add bond future first notice date zone column
  ALTER TABLE sec_future ADD bondFutureFirstNoticeDate_zone VARCHAR(50) NULL;
  
  -- add bond future first notice date column
  ALTER TABLE sec_future ADD bondFutureLastNoticeDate DATETIME2(6) NULL;
  
  -- add bond future last notice date zone column
  ALTER TABLE sec_future ADD bondFutureLastNoticeDate_zone VARCHAR(50) NULL;
  
COMMIT;
