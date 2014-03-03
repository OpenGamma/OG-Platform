-- -- DROP TABLE IF EXISTS CFG_SCHEMA_VERSION;
-- DROP TABLE CFG_SCHEMA_VERSION;
-- -- DROP SEQUENCE IF EXISTS CFG_CONFIG_SEQ;
-- DROP SEQUENCE CFG_CONFIG_SEQ;
-- -- DROP TABLE IF EXISTS CFG_CONFIG;
-- DROP TABLE CFG_CONFIG;

CREATE TABLE CFG_SCHEMA_VERSION
(
   VERSION_KEY NVARCHAR2(32) NOT NULL,
   VERSION_VALUE NVARCHAR2(255) NOT NULL
);
INSERT INTO cfg_schema_version (version_key, version_value) VALUES ('schema_patch', '48');

CREATE SEQUENCE CFG_CONFIG_SEQ
    START WITH 1000 INCREMENT BY 1 NOCYCLE;


CREATE TABLE CFG_CONFIG
(
   ID decimal(19) PRIMARY KEY NOT NULL,
   OID decimal(19) NOT NULL,
   ver_from_instant timestamp NOT NULL,
   ver_to_instant timestamp NOT NULL,
   corr_from_instant timestamp NOT NULL,
   corr_to_instant timestamp NOT NULL,
   NAME NVARCHAR2(255) NOT NULL,
   CONFIG_TYPE NVARCHAR2(255) NOT NULL,
   CONFIG blob NOT NULL,
   CONSTRAINT cfg_chk_config_ver_order CHECK (ver_from_instant <= ver_to_instant),
   CONSTRAINT cfg_chk_config_corr_order CHECK (corr_from_instant <= corr_to_instant)
);

CREATE INDEX ix_cfg_config_oid_instants ON cfg_config(oid, ver_from_instant, corr_from_instant);
CREATE INDEX ix_cfg_config_ver_instants ON cfg_config(ver_from_instant, ver_to_instant);
CREATE INDEX ix_cfg_config_corr_instants ON cfg_config(corr_from_instant, corr_to_instant);
CREATE INDEX ix_cfg_config_name_type ON cfg_config(name, config_type);
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);
CREATE INDEX ix_cfg_config_type_dates ON cfg_config(config_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
