START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='52' WHERE version_key='schema_patch';
  
  ALTER TABLE sec_bond ALTER COLUMN interestaccrual_date DROP NOT NULL;
  ALTER TABLE sec_bond ALTER COLUMN interestaccrual_zone DROP NOT NULL;
  ALTER TABLE sec_bond ALTER COLUMN firstcoupon_date DROP NOT NULL;
  ALTER TABLE sec_bond ALTER COLUMN firstcoupon_zone DROP NOT NULL;
  
COMMIT;
