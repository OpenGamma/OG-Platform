-- create-db-config.sql: Config Master

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row
CREATE TABLE cfg_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO cfg_schema_version (version_key, version_value) VALUES ('schema_patch', '48');

CREATE SEQUENCE cfg_config_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE cfg_config (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    config_type varchar(255) NOT NULL,
    config bytea NOT NULL,
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
