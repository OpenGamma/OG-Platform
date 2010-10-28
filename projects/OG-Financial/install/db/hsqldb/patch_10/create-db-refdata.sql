
-- create-db-refdata.sql

-- Holiday Master design has one document
--  holiday and associated dates
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

create sequence hol_holiday_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

create table hol_holiday (
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
    constraint hol_chk_holiday_ver_order check (ver_from_instant <= ver_to_instant)
);

create table hol_date (
    holiday_id bigint not null,
    hol_date date not null,
    constraint hol_fk_date2hol foreign key (holiday_id) references hol_holiday (id)
);

create index ix_hol_holiday_oid on hol_holiday(oid);
create index ix_hol_holiday_type on hol_holiday(hol_type);
