START TRANSACTION;
  UPDATE cfg_schema_version SET version_value='48' WHERE version_key='schema_patch';
  
  DROP INDEX ix_cfg_config_oid;
  DROP INDEX ix_cfg_config_ver_from_instant;
  DROP INDEX ix_cfg_config_ver_to_instant;
  DROP INDEX ix_cfg_config_corr_from_instant;
  DROP INDEX ix_cfg_config_corr_to_instant;
  DROP INDEX ix_cfg_config_name;
  
  CREATE INDEX ix_cfg_config_oid_instants ON cfg_config(oid, ver_from_instant, corr_from_instant);
  CREATE INDEX ix_cfg_config_ver_instants ON cfg_config(ver_from_instant, ver_to_instant);
  CREATE INDEX ix_cfg_config_corr_instants ON cfg_config(corr_from_instant, corr_to_instant);
  CREATE INDEX ix_cfg_config_name_type ON cfg_config(name, config_type);
  CREATE INDEX ix_cfg_config_type_dates ON cfg_config(config_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
  
COMMIT;