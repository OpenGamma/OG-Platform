-- create-db-config.sql: Config Master

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE cfg_config_seq AS bigint
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
    config blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cfg_chk_config_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT cfg_chk_config_corr_order CHECK (corr_from_instant <= corr_to_instant),
    CONSTRAINT name_type_unique UNIQUE (name, config_type, ver_to_instant) -- TODO this is not right IGN-101
);
CREATE INDEX ix_cfg_config_oid ON cfg_config(oid);
CREATE INDEX ix_cfg_config_ver_from_instant ON cfg_config(ver_from_instant);
CREATE INDEX ix_cfg_config_ver_to_instant ON cfg_config(ver_to_instant);
CREATE INDEX ix_cfg_config_corr_from_instant ON cfg_config(corr_from_instant);
CREATE INDEX ix_cfg_config_corr_to_instant ON cfg_config(corr_to_instant);
CREATE INDEX ix_cfg_config_name ON cfg_config(name);
-- CREATE INDEX ix_cfg_config_nameu ON cfg_config(UPPER(name));
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);
