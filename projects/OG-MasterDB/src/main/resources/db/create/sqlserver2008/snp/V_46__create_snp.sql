
-- create-db-marketdatasnapshot.sql

-- MarketDataSnapshotMaster design has one document
--  snapshot
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE snp_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO snp_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

-- CREATE SEQUENCE snp_snapshot_seq
--    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as BIGINT" required by Derby/HSQL, not accepted by Postgresql
CREATE TABLE snp_snapshot_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)

CREATE TABLE snp_snapshot (
    id BIGINT NOT NULL,
    oid BIGINT NOT NULL,
    ver_from_instant DATETIME2(6) NOT NULL,
    ver_to_instant DATETIME2(6) NOT NULL,
    corr_from_instant DATETIME2(6) NOT NULL,
    corr_to_instant DATETIME2(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    snapshot_type VARCHAR(255) NOT NULL,
    uname AS UPPER(name),
    time_zone VARCHAR(255),
    detail IMAGE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT snp_chk_snapshot_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT snp_chk_snapshot_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_snp_snapshot_oid ON snp_snapshot(oid);
CREATE INDEX ix_snp_snapshot_ver_from_instant ON snp_snapshot(ver_from_instant);
CREATE INDEX ix_snp_snapshot_ver_to_instant ON snp_snapshot(ver_to_instant);
CREATE INDEX ix_snp_snapshot_corr_from_instant ON snp_snapshot(corr_from_instant);
CREATE INDEX ix_snp_snapshot_corr_to_instant ON snp_snapshot(corr_to_instant);
CREATE INDEX ix_snp_snapshot_name ON snp_snapshot(name);
CREATE INDEX ix_snp_snapshot_nameu ON snp_snapshot(uname);
CREATE INDEX ix_snp_snapshot_name_type ON snp_snapshot(name, snapshot_type);
