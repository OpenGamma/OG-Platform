SET XACT_ABORT ON;
BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='52' WHERE version_key='schema_patch';

  ALTER TABLE sec_bond ALTER COLUMN interestaccrual_date DATETIME2(6);
  ALTER TABLE sec_bond ALTER COLUMN interestaccrual_zone VARCHAR(50);
  ALTER TABLE sec_bond ALTER COLUMN firstcoupon_date DATETIME2(6);
  ALTER TABLE sec_bond ALTER COLUMN firstcoupon_zone VARCHAR(50);
    
COMMIT;
