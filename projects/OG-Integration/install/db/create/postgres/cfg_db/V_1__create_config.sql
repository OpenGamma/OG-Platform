-- create-db-config.sql: Config Master

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE cfg_config_seq
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE cfg_config (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    config_type varchar(255) not null,
    config bytea not null,
    primary key (id),
    constraint cfg_chk_config_ver_order check (ver_from_instant <= ver_to_instant),
    constraint cfg_chk_config_corr_order check (corr_from_instant <= corr_to_instant),
	constraint name_type_unique unique (name, config_type, ver_to_instant) -- TODO this is not right IGN-101
);
CREATE INDEX ix_cfg_config_oid ON cfg_config(oid);
CREATE INDEX ix_cfg_config_ver_from_instant ON cfg_config(ver_from_instant);
CREATE INDEX ix_cfg_config_ver_to_instant ON cfg_config(ver_to_instant);
CREATE INDEX ix_cfg_config_corr_from_instant ON cfg_config(corr_from_instant);
CREATE INDEX ix_cfg_config_corr_to_instant ON cfg_config(corr_to_instant);
CREATE INDEX ix_cfg_config_name ON cfg_config(name);
CREATE INDEX ix_cfg_config_nameu ON cfg_config(upper(name));
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);