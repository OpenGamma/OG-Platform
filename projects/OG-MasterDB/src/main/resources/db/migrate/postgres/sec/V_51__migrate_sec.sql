START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='51' WHERE version_key='schema_patch';
  
  ALTER TABLE sec_bond ALTER COLUMN settlement_date DROP NOT NULL;
  ALTER TABLE sec_bond ALTER COLUMN settlement_zone DROP NOT NULL;
  ALTER TABLE sec_bond ALTER COLUMN issuanceprice DROP NOT NULL;
  
COMMIT;
