-- create-db-config.sql: Config Master

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as BIGINT" required by Derby/HSQL, not accepted by Postgresql
-- CREATE SEQUENCE cfg_config_seq
CREATE table cfg_config_seq (
  SeqID int identity(1000,1) primary key,
  SeqVal VARCHAR(1)
)

CREATE TABLE cfg_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO cfg_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

CREATE TABLE cfg_config (
    id BIGINT NOT NULL,
    oid BIGINT NOT NULL,
    ver_from_instant DATETIME2(6) NOT NULL,
    ver_to_instant DATETIME2(6) NOT NULL,
    corr_from_instant DATETIME2(6) NOT NULL,
    corr_to_instant DATETIME2(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    uname AS UPPER(name),
    config_type VARCHAR(255) NOT NULL,
    config IMAGE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cfg_chk_config_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT cfg_chk_config_corr_order CHECK (corr_from_instant <= corr_to_instant),
    CONSTRAINT name_type_unique UNIQUE (name, config_type, corr_from_instant, ver_from_instant), 
    CONSTRAINT oid_unique UNIQUE (oid, corr_from_instant, ver_from_instant) 
);
CREATE INDEX ix_cfg_config_oid ON cfg_config(oid);
CREATE INDEX ix_cfg_config_ver_from_instant ON cfg_config(ver_from_instant);
CREATE INDEX ix_cfg_config_ver_to_instant ON cfg_config(ver_to_instant);
CREATE INDEX ix_cfg_config_corr_from_instant ON cfg_config(corr_from_instant);
CREATE INDEX ix_cfg_config_corr_to_instant ON cfg_config(corr_to_instant);
CREATE INDEX ix_cfg_config_name ON cfg_config(name);
CREATE INDEX ix_cfg_config_nameu ON cfg_config(uname);
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);

