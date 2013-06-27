BEGIN TRAN;

  UPDATE cfg_schema_version SET version_value='48' WHERE version_key='schema_patch';
  
  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_oid')
    DROP INDEX ix_cfg_config_oid ON cfg_config;

  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_ver_from_instant')
    DROP INDEX ix_cfg_config_ver_from_instant ON cfg_config;

  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_ver_from_instant')
    DROP INDEX ix_cfg_config_corr_from_instant ON cfg_config;

  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_corr_from_instant')
    DROP INDEX ix_cfg_config_corr_from_instant ON cfg_config;

  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_corr_to_instant')
    DROP INDEX ix_cfg_config_corr_to_instant ON cfg_config;

  IF EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_name')
    DROP INDEX ix_cfg_config_name ON cfg_config;


  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_oid_instants')
    CREATE INDEX ix_cfg_config_oid_instants ON cfg_config(oid, ver_from_instant, corr_from_instant);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_ver_instants')
    CREATE INDEX ix_cfg_config_ver_instants ON cfg_config(ver_from_instant, ver_to_instant);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_corr_instants')
    CREATE INDEX ix_cfg_config_corr_instants ON cfg_config(corr_from_instant, corr_to_instant);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_name_type')
    CREATE INDEX ix_cfg_config_name_type ON cfg_config(name, config_type);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_cfg_config_type_dates')
    CREATE INDEX ix_cfg_config_type_dates ON cfg_config(config_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);



COMMIT;
