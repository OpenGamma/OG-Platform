--DROP TABLE hol_schema_version;
--DROP SEQUENCE hol_holiday_seq;
--DROP TABLE hol_holiday;
--DROP TABLE hol_date;
--DROP TABLE exg_schema_version;
--DROP SEQUENCE exg_exchange_seq;
--DROP SEQUENCE exg_idkey_seq;
--DROP TABLE exg_exchange;
--DROP TABLE exg_idkey;
--DROP TABLE exg_exchange2idkey;
--


-- Holiday Master design has one document
--  holiday and associated dates
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row


CREATE TABLE hol_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO hol_schema_version (version_key, version_value) VALUES ('schema_patch', '46');


CREATE SEQUENCE hol_holiday_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;


CREATE TABLE hol_holiday (
    id decimal(19) PRIMARY KEY NOT NULL,
    oid decimal(19) NOT NULL,
    ver_from_instant timestamp NOT NULL,
    ver_to_instant timestamp NOT NULL,
    corr_from_instant timestamp NOT NULL,
    corr_to_instant timestamp NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    provider_scheme NVARCHAR2(255),
    provider_value NVARCHAR2(255),
    hol_type NVARCHAR2(255) NOT NULL,
    region_scheme NVARCHAR2(255),
    region_value NVARCHAR2(255),
    exchange_scheme NVARCHAR2(255),
    exchange_value NVARCHAR2(255),
    custom_scheme NVARCHAR2(255),
    custom_value NVARCHAR2(255),
    currency_iso NVARCHAR2(255),
    CONSTRAINT hol_chk_holiday_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT hol_chk_holiday_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_hol_holiday_oid ON hol_holiday(oid);
CREATE INDEX ix_hol_holiday_ver_from ON hol_holiday(ver_from_instant);
CREATE INDEX ix_hol_holiday_ver_to ON hol_holiday(ver_to_instant);
CREATE INDEX ix_hol_holiday_corr_from ON hol_holiday(corr_from_instant);
CREATE INDEX ix_hol_holiday_corr_to ON hol_holiday(corr_to_instant);
CREATE INDEX ix_hol_holiday_name ON hol_holiday(name);
CREATE INDEX ix_hol_holiday_nameu ON hol_holiday(UPPER(name));
CREATE INDEX ix_hol_holiday_provider_scheme ON hol_holiday(provider_scheme);
CREATE INDEX ix_hol_holiday_provider_value ON hol_holiday(provider_value);
CREATE INDEX ix_hol_holiday_holiday_type ON hol_holiday(hol_type);
CREATE INDEX ix_hol_holiday_region_scheme ON hol_holiday(region_scheme);
CREATE INDEX ix_hol_holiday_region_value ON hol_holiday(region_value);
CREATE INDEX ix_hol_holiday_exchange_scheme ON hol_holiday(exchange_scheme);
CREATE INDEX ix_hol_holiday_exchange_value ON hol_holiday(exchange_value);
CREATE INDEX ix_hol_holiday_custom_scheme ON hol_holiday(custom_scheme);
CREATE INDEX ix_hol_holiday_custom_value ON hol_holiday(custom_value);
CREATE INDEX ix_hol_holiday_currency_iso ON hol_holiday(currency_iso);

CREATE TABLE hol_date (
    holiday_id decimal(19) NOT NULL,
    hol_date date NOT NULL,
    CONSTRAINT hol_fk_date2hol FOREIGN KEY (holiday_id) REFERENCES hol_holiday (id)
);
CREATE INDEX ix_hol_date_holiday_id ON hol_date(holiday_id);


-- Exchange Master design has one document
--  exchange and associated identifiers
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE exg_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO exg_schema_version (version_key, version_value) VALUES ('schema_patch', '46');


CREATE SEQUENCE exg_exchange_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;


CREATE SEQUENCE exg_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;



CREATE TABLE exg_exchange (
    id decimal(19) NOT NULL,
    oid decimal(19) NOT NULL,
    ver_from_instant timestamp NOT NULL,
    ver_to_instant timestamp NOT NULL,
    corr_from_instant timestamp NOT NULL,
    corr_to_instant timestamp NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    time_zone NVARCHAR2(255),
    detail BLOB NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT exg_chk_exchange_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT exg_chk_exchange_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_exg_exchange_oid ON exg_exchange(oid);
CREATE INDEX ix_exg_exchange_ver_from ON exg_exchange(ver_from_instant);
CREATE INDEX ix_exg_exchange_ver_to ON exg_exchange(ver_to_instant);
CREATE INDEX ix_exg_exchange_corr_from ON exg_exchange(corr_from_instant);
CREATE INDEX ix_exg_exchange_corr_to ON exg_exchange(corr_to_instant);
CREATE INDEX ix_exg_exchange_name ON exg_exchange(name);
CREATE INDEX ix_exg_exchange_nameu ON exg_exchange(UPPER(name));


CREATE TABLE exg_idkey (
    id decimal(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT exg_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE exg_exchange2idkey (
    exchange_id decimal(19) NOT NULL,
    idkey_id decimal(19) NOT NULL,
    PRIMARY KEY (exchange_id, idkey_id),
    CONSTRAINT exg_fk_exgidkey2exg FOREIGN KEY (exchange_id) REFERENCES exg_exchange (id),
    CONSTRAINT exg_fk_exgidkey2idkey FOREIGN KEY (idkey_id) REFERENCES exg_idkey (id)
);
CREATE INDEX ix_exg_exg2idkey_idkey ON exg_exchange2idkey(idkey_id);
-- exg_exchange2idkey is fully dependent of exg_exchange
