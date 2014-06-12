BEGIN TRAN;

  UPDATE hol_schema_version SET version_value='46' WHERE version_key='schema_patch';

  ALTER TABLE hol_holiday ADD custom_scheme VARCHAR(255) NULL;
  ALTER TABLE hol_holiday ADD custom_value VARCHAR(255) NULL;
  CREATE INDEX ix_hol_holiday_custom_scheme ON hol_holiday(custom_scheme);
  CREATE INDEX ix_hol_holiday_custom_value ON hol_holiday(custom_value);


  UPDATE exg_schema_version SET version_value='46' WHERE version_key='schema_patch';

COMMIT;
