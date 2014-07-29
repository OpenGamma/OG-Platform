
-- Joda-Bean Legal Entity Master

-- design is document-based
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE len_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO len_schema_version (version_key, version_value) VALUES ('schema_patch', '1');

CREATE SEQUENCE len_doc_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE len_idkey_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE len_attr_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE len_prop_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql


-- Main document table
------------------------
CREATE TABLE len_document(
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    main_type char NOT NULL,
    sub_type varchar(255) NOT NULL,
    actual_type varchar(255) NOT NULL,
    packed_data bytea NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT len_fk_doc2doc FOREIGN KEY (oid) REFERENCES len_document (id),
    CONSTRAINT len_chk_doc_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT len_chk_doc_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_len_doc_oid_instants ON len_document(oid, ver_from_instant, corr_from_instant);
CREATE INDEX ix_len_doc_ver_instants ON len_document(ver_from_instant, ver_to_instant);
CREATE INDEX ix_len_doc_corr_instants ON len_document(corr_from_instant, corr_to_instant);
CREATE INDEX ix_len_doc_name_type ON len_document(name, main_type, sub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);
CREATE INDEX ix_len_doc_sub_type ON len_document(sub_type, ver_from_instant, corr_from_instant, ver_to_instant, corr_to_instant);


-- Document external ID
-------------------------
CREATE TABLE len_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT len_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE len_doc2idkey (
    doc_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (doc_id, idkey_id),
    CONSTRAINT len_fk_docidkey2doc FOREIGN KEY (doc_id) REFERENCES len_document (id),
    CONSTRAINT len_fk_docidkey2idkey FOREIGN KEY (idkey_id) REFERENCES len_idkey (id)
);
CREATE INDEX ix_len_doc2idkey_idkey ON len_doc2idkey(idkey_id);
-- len_doc2idkey is fully dependent of len_document


-- Document attributes
------------------------
CREATE TABLE len_attr (
    id bigint NOT NULL,
    attr_key varchar(255) NOT NULL,
    attr_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT len_chk_attr UNIQUE (attr_key, attr_value)
);

CREATE TABLE len_doc2attr (
    doc_id bigint NOT NULL,
    attr_id bigint NOT NULL,
    PRIMARY KEY (doc_id, attr_id),
    CONSTRAINT len_fk_docattr2doc FOREIGN KEY (doc_id) REFERENCES len_document (id),
    CONSTRAINT len_fk_docattr2attr FOREIGN KEY (attr_id) REFERENCES len_attr (id)
);
CREATE INDEX ix_len_doc2attr_attr ON len_doc2attr(attr_id);
-- len_doc2attr is fully dependent of len_document


-- Document searchable properties
---------------------------------
CREATE TABLE len_prop (
    id bigint NOT NULL,
    prop_key varchar(255) NOT NULL,
    prop_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT len_chk_prop UNIQUE (prop_key, prop_value)
);

CREATE TABLE len_doc2prop (
    doc_id bigint NOT NULL,
    prop_id bigint NOT NULL,
    PRIMARY KEY (doc_id, prop_id),
    CONSTRAINT len_fk_docprop2doc FOREIGN KEY (doc_id) REFERENCES len_document (id),
    CONSTRAINT len_fk_docprop2prop FOREIGN KEY (prop_id) REFERENCES len_prop (id)
);
CREATE INDEX ix_len_doc2prop_prop ON len_doc2prop(prop_id);
-- len_doc2prop is fully dependent of len_document
