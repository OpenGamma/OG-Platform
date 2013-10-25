
-- Joda-Bean Security Master

-- design is document-based
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE secb_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO secb_schema_version (version_key, version_value) VALUES ('schema_patch', '1');

-- CREATE SEQUENCE secb_doc_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- CREATE SEQUENCE secb_idkey_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- CREATE SEQUENCE secb_attr_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- CREATE SEQUENCE secb_prop_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE TABLE secb_doc_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)
CREATE TABLE secb_idkey_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)
CREATE TABLE secb_attr_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)
CREATE TABLE secb_prop_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)


-- Main document table
------------------------
CREATE TABLE secb_document(
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant DATETIME2(6) NOT NULL,
    ver_to_instant DATETIME2(6) NOT NULL,
    corr_from_instant DATETIME2(6) NOT NULL,
    corr_to_instant DATETIME2(6) NOT NULL,
    name varchar(255) NOT NULL,
    uname AS UPPER(name),
    main_type char NOT NULL,
    sub_type varchar(255) NOT NULL,
    usub_type AS UPPER(sub_type),
    actual_type varchar(255) NOT NULL,
    packed_data varbinary(max) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT secb_fk_doc2doc FOREIGN KEY (oid) REFERENCES secb_document (id),
    CONSTRAINT secb_chk_doc_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT secb_chk_doc_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_secb_doc_oid_instants ON secb_document(oid, ver_from_instant, corr_from_instant);
CREATE INDEX ix_secb_doc_ver_instants ON secb_document(ver_from_instant, ver_to_instant);
CREATE INDEX ix_secb_doc_corr_instants ON secb_document(corr_from_instant, corr_to_instant);
CREATE INDEX ix_secb_doc_name_type ON secb_document(uname, main_type, sub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
CREATE INDEX ix_secb_doc_sub_type ON secb_document(usub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);


-- Document external ID
-------------------------
CREATE TABLE secb_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT secb_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE secb_doc2idkey (
    doc_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (doc_id, idkey_id),
    CONSTRAINT secb_fk_docidkey2doc FOREIGN KEY (doc_id) REFERENCES secb_document (id),
    CONSTRAINT secb_fk_docidkey2idkey FOREIGN KEY (idkey_id) REFERENCES secb_idkey (id)
);
CREATE INDEX ix_secb_doc2idkey_idkey ON secb_doc2idkey(idkey_id);
-- secb_doc2idkey is fully dependent of secb_document


-- Document attributes
------------------------
CREATE TABLE secb_attr (
    id bigint NOT NULL,
    attr_key varchar(255) NOT NULL,
    attr_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT secb_chk_attr UNIQUE (attr_key, attr_value)
);

CREATE TABLE secb_doc2attr (
    doc_id bigint NOT NULL,
    attr_id bigint NOT NULL,
    PRIMARY KEY (doc_id, attr_id),
    CONSTRAINT secb_fk_docattr2doc FOREIGN KEY (doc_id) REFERENCES secb_document (id),
    CONSTRAINT secb_fk_docattr2attr FOREIGN KEY (attr_id) REFERENCES secb_attr (id)
);
CREATE INDEX ix_secb_doc2attr_attr ON secb_doc2attr(attr_id);
-- secb_doc2attr is fully dependent of secb_document


-- Document searchable properties
---------------------------------
CREATE TABLE secb_prop (
    id bigint NOT NULL,
    prop_key varchar(255) NOT NULL,
    prop_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT secb_chk_prop UNIQUE (prop_key, prop_value)
);

CREATE TABLE secb_doc2prop (
    doc_id bigint NOT NULL,
    prop_id bigint NOT NULL,
    PRIMARY KEY (doc_id, prop_id),
    CONSTRAINT secb_fk_docprop2doc FOREIGN KEY (doc_id) REFERENCES secb_document (id),
    CONSTRAINT secb_fk_docprop2prop FOREIGN KEY (prop_id) REFERENCES secb_prop (id)
);
CREATE INDEX ix_secb_doc2prop_prop ON secb_doc2prop(prop_id);
-- secb_doc2prop is fully dependent of secb_document
