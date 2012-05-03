START TRANSACTION;
  UPDATE pos_schema_version SET version_value='46' WHERE version_key='schema_patch';

  DROP   INDEX ix_pos_trade_attr_key;
  ALTER  TABLE pos_trade_attribute DROP CONSTRAINT pos_chk_uq_trade_attribute;
  ALTER  TABLE pos_trade_attribute RENAME COLUMN key TO attr_key;
  ALTER  TABLE pos_trade_attribute RENAME COLUMN value TO attr_value;
  ALTER  TABLE pos_trade_attribute ADD CONSTRAINT pos_chk_uq_trade_attribute UNIQUE (trade_id, attr_key, attr_value);
  CREATE INDEX ix_pos_trade_attr_key ON pos_trade_attribute(attr_key);

  DROP   INDEX ix_pos_attr_key;
  ALTER  TABLE pos_attribute DROP CONSTRAINT pos_chk_uq_pos_attribute;
  ALTER  TABLE pos_attribute RENAME COLUMN key TO attr_key;
  ALTER  TABLE pos_attribute RENAME COLUMN value TO attr_value;
  ALTER  TABLE pos_trade_attribute ADD CONSTRAINT pos_chk_uq_pos_attribute UNIQUE (id, attr_key, attr_value);
  CREATE INDEX ix_pos_attr_key ON pos_attribute(attr_key);
COMMIT;
