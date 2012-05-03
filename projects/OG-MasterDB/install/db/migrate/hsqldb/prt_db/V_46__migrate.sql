START TRANSACTION;
  UPDATE prt_schema_version SET version_value='46' WHERE version_key='schema_patch';

  DROP   INDEX ix_prt_attr_key;
  ALTER  TABLE prt_portfolio_attribute DROP CONSTRAINT prt_chk_uq_prt_attribute;
  ALTER  TABLE prt_portfolio_attribute ALTER COLUMN key RENAME TO attr_key;
  ALTER  TABLE prt_portfolio_attribute ALTER COLUMN value RENAME TO attr_value;
  ALTER  TABLE prt_portfolio_attribute ADD CONSTRAINT prt_chk_uq_prt_attribute UNIQUE (portfolio_id, attr_key, attr_value);
  CREATE INDEX ix_prt_attr_key ON prt_portfolio_attribute(attr_key);

COMMIT;
