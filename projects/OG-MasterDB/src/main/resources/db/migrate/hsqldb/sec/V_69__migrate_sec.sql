START TRANSACTION;
  -- update the version
  UPDATE sec_schema_version SET version_value='69' WHERE version_key='schema_patch';

  -- add bond future first notice date column
  ALTER TABLE sec_future ADD bondFutureFirstNoticeDate timestamp without time zone NULL;
  
  -- add bond future first notice date zone column
  ALTER TABLE sec_future ADD bondFutureFirstNoticeDate_zone varchar(50) NULL;
  
  -- add bond future last notice date column
  ALTER TABLE sec_future ADD bondFutureLastNoticeDate timestamp without time zone zone NULL;
  
  -- add bond future last notice date zone column
  ALTER TABLE sec_future ADD bondFutureLastNoticeDate_zone varchar(50) NULL;
  
COMMIT;
