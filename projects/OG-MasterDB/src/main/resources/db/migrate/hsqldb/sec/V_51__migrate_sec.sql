START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='51' WHERE version_key='schema_patch';

  ALTER TABLE sec_bond ALTER COLUMN settlement_date SET NULL;
  ALTER TABLE sec_bond ALTER COLUMN settlement_zone SET NULL;
  ALTER TABLE sec_bond ALTER COLUMN issuanceprice SET NULL;
  
COMMIT;
