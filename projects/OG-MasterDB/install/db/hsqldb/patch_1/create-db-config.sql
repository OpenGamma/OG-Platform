-- create-db-config.sql: Config Master

-- design has one document
--  config
-- unitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

create sequence cfg_config_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

create table cfg_config (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    name varchar(255) not null,
    config_type varchar(255) not null,
    config blob not null,
    primary key (id),
    constraint cfg_chk_config_ver_order check (ver_from_instant <= ver_to_instant)
);

create index ix_cfg_config_oid on cfg_config(oid);
create index ix_cfg_config_config_type on cfg_config(config_type);
