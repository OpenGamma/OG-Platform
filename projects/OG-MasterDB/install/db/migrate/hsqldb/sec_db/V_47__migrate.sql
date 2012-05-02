START TRANSACTION;
  -- update the version
  UPDATE sec_schema_version SET version_value='47' WHERE version_key='schema_patch';

  DROP   INDEX ix_sec_security_attr_key;
  ALTER  TABLE sec_security_attribute DROP CONSTRAINT sec_chk_uq_security_attribute;
  ALTER  TABLE sec_security_attribute ALTER COLUMN key RENAME TO attr_key;
  ALTER  TABLE sec_security_attribute ALTER COLUMN value RENAME TO attr_value;
  ALTER  TABLE sec_security_attribute ADD CONSTRAINT sec_chk_uq_security_attribute UNIQUE (security_id, attr_key, attr_value);
  CREATE INDEX ix_sec_security_attr_key ON sec_security_attribute(attr_key);
COMMIT;
