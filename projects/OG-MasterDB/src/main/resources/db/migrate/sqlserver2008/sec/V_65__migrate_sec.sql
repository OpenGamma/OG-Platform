BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='65' WHERE version_key='schema_patch';

  -- add column as nullable (not our ultimate goal)
  ALTER TABLE sec_credit_default_swap_index_definition ADD recovery_rate double precision NULL;

  -- set the value to 0 for any existing data
  UPDATE sec_credit_default_swap_index_definition SET recovery_rate = 0;

  -- now add in the not null constraint
  ALTER TABLE sec_credit_default_swap_index_definition ALTER COLUMN recovery_rate double precision NOT NULL;

COMMIT;
