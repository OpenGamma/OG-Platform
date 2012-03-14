-- create-db-marketdatasnapshot.sql

-- MarketDataSnapshotMaster design has one document
--  snapshot
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE snp_snapshot_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE snp_snapshot (
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
    constraint snp_chk_snapshot_ver_order check (ver_from_instant <= ver_to_instant),
    constraint snp_chk_snapshot_corr_order check (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_snp_snapshot_oid ON snp_snapshot(oid);
CREATE INDEX ix_snp_snapshot_ver_from_instant ON snp_snapshot(ver_from_instant);
CREATE INDEX ix_snp_snapshot_ver_to_instant ON snp_snapshot(ver_to_instant);
CREATE INDEX ix_snp_snapshot_corr_from_instant ON snp_snapshot(corr_from_instant);
CREATE INDEX ix_snp_snapshot_corr_to_instant ON snp_snapshot(corr_to_instant);
CREATE INDEX ix_snp_snapshot_name ON snp_snapshot(name);