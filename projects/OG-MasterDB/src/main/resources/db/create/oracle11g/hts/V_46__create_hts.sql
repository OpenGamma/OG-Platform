-- create-db-historicaltimeseries.sql: Historical time-series Master

-- design has one main document with data points handled separately
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

-- Data point versioning is slightly different.
-- Data points are inserted on a daily basis with a single version instant.
-- There may be a delay between the value becoming available and the insertion
-- which the version instant models, ensuring the exact state previously viewed.
-- A new version of a point may not be created (the insertion of the point
-- is the versioned item, and treated as being at the document level).
-- A data point may however be corrected. A single instant recorded for this.
-- The actual data point is the latest matching these criteria:
--  hts_point.ver_instant <= search_version_instant &&
--  hts_point.corr_instant <= search_correction_instant

CREATE TABLE hts_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO hts_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

CREATE SEQUENCE hts_master_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE hts_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE hts_doc2idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE hts_dimension_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE hts_name (
    id NUMBER(19) NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_hts_name_name ON hts_name(name);

CREATE TABLE hts_data_field (
    id NUMBER(19) NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_hts_data_field_name ON hts_data_field(name);

CREATE TABLE hts_data_source (
    id NUMBER(19) NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_hts_data_source_name ON hts_data_source(name);

CREATE TABLE hts_data_provider (
    id NUMBER(19) NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_hts_data_provider_name ON hts_data_provider(name);

CREATE TABLE hts_observation_time (
    id NUMBER(19) NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_hts_observation_time_name ON hts_observation_time(name);

CREATE TABLE hts_document (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    name_id NUMBER(19) NOT NULL,
    data_field_id NUMBER(19) NOT NULL,
    data_source_id NUMBER(19) NOT NULL,
    data_provider_id NUMBER(19) NOT NULL,
    observation_time_id NUMBER(19) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_fk_doc2doc FOREIGN KEY (oid) REFERENCES hts_document (id),
    CONSTRAINT hts_chk_doc_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT hts_chk_doc_corr_order CHECK (corr_from_instant <= corr_to_instant),
    CONSTRAINT hts_fk_doc2name FOREIGN KEY (name_id) REFERENCES hts_name (id),
    CONSTRAINT hts_fk_doc2data_field FOREIGN KEY (data_field_id) REFERENCES hts_data_field (id),
    CONSTRAINT hts_fk_doc2data_source FOREIGN KEY (data_source_id) REFERENCES hts_data_source (id),
    CONSTRAINT hts_fk_doc2data_provider FOREIGN KEY (data_provider_id) REFERENCES hts_data_provider (id),
    CONSTRAINT hts_fk_doc2observation_time FOREIGN KEY (observation_time_id) REFERENCES hts_observation_time (id)
);
CREATE INDEX ix_hts_hts_oid ON hts_document(oid);
CREATE INDEX ix_hts_hts_ver_from ON hts_document(ver_from_instant);
CREATE INDEX ix_hts_hts_ver_to ON hts_document(ver_to_instant);
CREATE INDEX ix_hts_hts_corr_from ON hts_document(corr_from_instant);
CREATE INDEX ix_hts_hts_corr_to ON hts_document(corr_to_instant);
CREATE INDEX ix_hts_hts_name_id ON hts_document(name_id);
CREATE INDEX ix_hts_hts_data_field ON hts_document(data_field_id);
CREATE INDEX ix_hts_hts_data_source ON hts_document(data_source_id);
CREATE INDEX ix_hts_hts_data_provider ON hts_document(data_provider_id);
CREATE INDEX ix_hts_hts_obs_time ON hts_document(observation_time_id);

CREATE TABLE hts_idkey (
    id NUMBER(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_chk_idkey UNIQUE (key_scheme, key_value)
);
CREATE INDEX ix_hts_key_value ON hts_idkey(key_value);


CREATE TABLE hts_doc2idkey (
    id NUMBER(19) NOT NULL,
    doc_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_fk_htsidkey2doc FOREIGN KEY (doc_id) REFERENCES hts_document (id),
    CONSTRAINT hts_fk_htsidkey2idkey FOREIGN KEY (idkey_id) REFERENCES hts_idkey (id),
    CONSTRAINT hts_chk_doc2idkey UNIQUE (doc_id, idkey_id, valid_from, valid_to)
);
CREATE INDEX ix_hts_doc2idkey_idkey ON hts_doc2idkey(idkey_id, valid_from, valid_to);
-- hts_doc2idkey is fully dependent of hts_document

CREATE TABLE hts_permission (
    id NUMBER(19) NOT NULL,
    doc_id NUMBER(19) NOT NULL,
    permission NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_fk_hts_permission2doc FOREIGN KEY (doc_id) REFERENCES hts_document (id),
    CONSTRAINT hts_chk_hts_permission UNIQUE (doc_id, permission)
);
-- hts_permission is fully dependent of hts_document

CREATE TABLE hts_point (
    doc_oid NUMBER(19) NOT NULL,
    point_date TIMESTAMP NOT NULL,
    ver_instant TIMESTAMP NOT NULL,
    corr_instant TIMESTAMP NOT NULL,
    point_value DOUBLE PRECISION,
    PRIMARY KEY (doc_oid, point_date, ver_instant, corr_instant)
);
-- null value used to indicate point was deleted
