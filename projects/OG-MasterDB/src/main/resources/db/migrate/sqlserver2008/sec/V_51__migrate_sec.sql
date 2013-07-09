SET XACT_ABORT ON;
BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='51' WHERE version_key='schema_patch';

  ALTER TABLE sec_bond ALTER COLUMN settlement_date DATETIME2(6);
  ALTER TABLE sec_bond ALTER COLUMN settlement_zone VARCHAR(50);
  ALTER TABLE sec_bond ALTER COLUMN issuanceprice double precision;
    
COMMIT;
