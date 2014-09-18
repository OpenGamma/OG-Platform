
-- Joda-Bean Convention Master

-- design is document-based
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE cnv_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO cnv_schema_version (version_key, version_value) VALUES ('schema_patch', '1');

CREATE SEQUENCE cnv_doc_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE cnv_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE cnv_attr_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE cnv_prop_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;


-- Main document table
------------------------
CREATE TABLE cnv_document(
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    name NVARCHAR2(255) NOT NULL,
    main_type CHAR NOT NULL,
    sub_type NVARCHAR2(255) NOT NULL,
    actual_type NVARCHAR2(255) NOT NULL,
    packed_data BLOB NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cnv_fk_doc2doc FOREIGN KEY (oid) REFERENCES cnv_document (id),
    CONSTRAINT cnv_chk_doc_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT cnv_chk_doc_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_cnv_doc_oid_instants ON cnv_document(oid, ver_from_instant, corr_from_instant);
CREATE INDEX ix_cnv_doc_ver_instants ON cnv_document(ver_from_instant, ver_to_instant);
CREATE INDEX ix_cnv_doc_corr_instants ON cnv_document(corr_from_instant, corr_to_instant);
CREATE INDEX ix_cnv_doc_name_type ON cnv_document(name, main_type, sub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
CREATE INDEX ix_cnv_doc_sub_type ON cnv_document(sub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);


-- Document external ID
-------------------------
CREATE TABLE cnv_idkey (
    id NUMBER(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cnv_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE cnv_doc2idkey (
    doc_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    PRIMARY KEY (doc_id, idkey_id),
    CONSTRAINT cnv_fk_docidkey2doc FOREIGN KEY (doc_id) REFERENCES cnv_document (id),
    CONSTRAINT cnv_fk_docidkey2idkey FOREIGN KEY (idkey_id) REFERENCES cnv_idkey (id)
);
CREATE INDEX ix_cnv_doc2idkey_idkey ON cnv_doc2idkey(idkey_id);
-- cnv_doc2idkey is fully dependent of cnv_document


-- Document attributes
------------------------
CREATE TABLE cnv_attr (
    id NUMBER(19) NOT NULL,
    attr_key NVARCHAR2(255) NOT NULL,
    attr_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cnv_chk_attr UNIQUE (attr_key, attr_value)
);

CREATE TABLE cnv_doc2attr (
    doc_id NUMBER(19) NOT NULL,
    attr_id NUMBER(19) NOT NULL,
    PRIMARY KEY (doc_id, attr_id),
    CONSTRAINT cnv_fk_docattr2doc FOREIGN KEY (doc_id) REFERENCES cnv_document (id),
    CONSTRAINT cnv_fk_docattr2attr FOREIGN KEY (attr_id) REFERENCES cnv_attr (id)
);
CREATE INDEX ix_cnv_doc2attr_attr ON cnv_doc2attr(attr_id);
-- cnv_doc2attr is fully dependent of cnv_document


-- Document searchable properties
---------------------------------
CREATE TABLE cnv_prop (
    id NUMBER(19) NOT NULL,
    prop_key NVARCHAR2(255) NOT NULL,
    prop_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cnv_chk_prop UNIQUE (prop_key, prop_value)
);

CREATE TABLE cnv_doc2prop (
    doc_id NUMBER(19) NOT NULL,
    prop_id NUMBER(19) NOT NULL,
    PRIMARY KEY (doc_id, prop_id),
    CONSTRAINT cnv_fk_docprop2doc FOREIGN KEY (doc_id) REFERENCES cnv_document (id),
    CONSTRAINT cnv_fk_docprop2prop FOREIGN KEY (prop_id) REFERENCES cnv_prop (id)
);
CREATE INDEX ix_cnv_doc2prop_prop ON cnv_doc2prop(prop_id);
-- cnv_doc2prop is fully dependent of cnv_document
