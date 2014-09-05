-- -- DROP TABLE IF EXISTS CFG_SCHEMA_VERSION;
-- DROP TABLE CFG_SCHEMA_VERSION;
-- -- DROP SEQUENCE IF EXISTS CFG_CONFIG_SEQ;
-- DROP SEQUENCE CFG_CONFIG_SEQ;
-- -- DROP TABLE IF EXISTS CFG_CONFIG;
-- DROP TABLE CFG_CONFIG;

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row
CREATE TABLE cfg_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO cfg_schema_version (version_key, version_value) VALUES ('schema_patch', '48');

CREATE SEQUENCE cfg_config_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;


CREATE TABLE cfg_config (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    config_type NVARCHAR2(255) NOT NULL,
    config blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cfg_chk_config_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT cfg_chk_config_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_cfg_config_oid_instants ON cfg_config(oid, ver_from_instant, corr_from_instant);
CREATE INDEX ix_cfg_config_ver_instants ON cfg_config(ver_from_instant, ver_to_instant);
CREATE INDEX ix_cfg_config_corr_instants ON cfg_config(corr_from_instant, corr_to_instant);
CREATE INDEX ix_cfg_config_name_type ON cfg_config(name, config_type);
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);
CREATE INDEX ix_cfg_config_type_dates ON cfg_config(config_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
