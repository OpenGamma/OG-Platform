START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='66' WHERE version_key='schema_patch';

  ALTER TABLE sec_credit_default_swap ALTER COLUMN recovery_rate DROP NOT NULL;

COMMIT;
