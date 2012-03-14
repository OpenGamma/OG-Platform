-- create-db-refdata.sql

-- Holiday Master design has one document
--  holiday and associated dates
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE hol_holiday_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE hol_holiday (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    provider_scheme varchar(255),
    provider_value varchar(255),
    hol_type varchar(255) not null,
    region_scheme varchar(255),
    region_value varchar(255),
    exchange_scheme varchar(255),
    exchange_value varchar(255),
    currency_iso varchar(255),
    primary key (id),
    constraint hol_chk_holiday_ver_order check (ver_from_instant <= ver_to_instant),
    constraint hol_chk_holiday_corr_order check (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_hol_holiday_oid ON hol_holiday(oid);
CREATE INDEX ix_hol_holiday_ver_from_instant ON hol_holiday(ver_from_instant);
CREATE INDEX ix_hol_holiday_ver_to_instant ON hol_holiday(ver_to_instant);
CREATE INDEX ix_hol_holiday_corr_from_instant ON hol_holiday(corr_from_instant);
CREATE INDEX ix_hol_holiday_corr_to_instant ON hol_holiday(corr_to_instant);
CREATE INDEX ix_hol_holiday_name ON hol_holiday(name);
-- CREATE INDEX ix_hol_holiday_nameu ON hol_holiday(upper(name));
CREATE INDEX ix_hol_holiday_provider_scheme ON hol_holiday(provider_scheme);
CREATE INDEX ix_hol_holiday_provider_value ON hol_holiday(provider_value);
CREATE INDEX ix_hol_holiday_holiday_type ON hol_holiday(hol_type);
CREATE INDEX ix_hol_holiday_region_scheme ON hol_holiday(region_scheme);
CREATE INDEX ix_hol_holiday_region_value ON hol_holiday(region_value);
CREATE INDEX ix_hol_holiday_exchange_scheme ON hol_holiday(exchange_scheme);
CREATE INDEX ix_hol_holiday_exchange_value ON hol_holiday(exchange_value);
CREATE INDEX ix_hol_holiday_currency_iso ON hol_holiday(currency_iso);

CREATE TABLE hol_date (
    holiday_id bigint not null,
    hol_date date not null,
    constraint hol_fk_date2hol foreign key (holiday_id) references hol_holiday (id)
);
CREATE INDEX ix_hol_date_holiday_id ON hol_date(holiday_id);


-- Exchange Master design has one document
--  exchange and associated identifiers
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE exg_exchange_seq as bigint
    start with 1000 increment by 1 no cycle;
CREATE SEQUENCE exg_idkey_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE exg_exchange (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    time_zone varchar(255),
    detail blob not null,
    primary key (id),
    constraint exg_chk_exchange_ver_order check (ver_from_instant <= ver_to_instant),
    constraint exg_chk_exchange_corr_order check (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_exg_exchange_oid ON exg_exchange(oid);
CREATE INDEX ix_exg_exchange_ver_from_instant ON exg_exchange(ver_from_instant);
CREATE INDEX ix_exg_exchange_ver_to_instant ON exg_exchange(ver_to_instant);
CREATE INDEX ix_exg_exchange_corr_from_instant ON exg_exchange(corr_from_instant);
CREATE INDEX ix_exg_exchange_corr_to_instant ON exg_exchange(corr_to_instant);
CREATE INDEX ix_exg_exchange_name ON exg_exchange(name);
-- CREATE INDEX ix_exg_exchange_nameu ON exg_exchange(upper(name));

CREATE TABLE exg_idkey (
    id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    primary key (id),
    constraint exg_chk_idkey unique (key_scheme, key_value)
);

CREATE TABLE exg_exchange2idkey (
    exchange_id bigint not null,
    idkey_id bigint not null,
    primary key (exchange_id, idkey_id),
    constraint exg_fk_exgidkey2exg foreign key (exchange_id) references exg_exchange (id),
    constraint exg_fk_exgidkey2idkey foreign key (idkey_id) references exg_idkey (id)
);
-- exg_exchange2idkey is fully dependent of exg_exchange