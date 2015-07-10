
-- create-db-marketdatasnapshot.sql

-- MarketDataSnapshotMaster design has one document
--  snapshot
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE snp_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO snp_schema_version (version_key, version_value) VALUES ('schema_patch', '45');

CREATE SEQUENCE snp_snapshot_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE snp_snapshot (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    time_zone NVARCHAR2(255),
    detail BLOB NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT snp_chk_snapshot_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT snp_chk_snapshot_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_snp_snapshot_oid ON snp_snapshot(oid);
CREATE INDEX ix_snp_snapshot_ver_from ON snp_snapshot(ver_from_instant);
CREATE INDEX ix_snp_snapshot_ver_to ON snp_snapshot(ver_to_instant);
CREATE INDEX ix_snp_snapshot_corr_from ON snp_snapshot(corr_from_instant);
CREATE INDEX ix_snp_snapshot_corr_to ON snp_snapshot(corr_to_instant);
CREATE INDEX ix_snp_snapshot_name ON snp_snapshot(name);
CREATE INDEX ix_snp_snapshot_nameu ON snp_snapshot(UPPER(name));
