BEGIN;

CREATE SEQUENCE hts_doc2idkey_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;

ALTER TABLE hts_doc2idkey
    DROP CONSTRAINT hts_fk_htsidkey2doc;

ALTER TABLE hts_doc2idkey
    DROP CONSTRAINT hts_fk_htsidkey2idkey;

ALTER TABLE hts_doc2idkey RENAME TO hts_doc2idkey_old;

CREATE TABLE hts_doc2idkey (
    id bigint NOT NULL DEFAULT nextval('hts_doc2idkey_seq'),
    doc_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    valid_from date NOT NULL,
    valid_to date NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_fk_htsidkey2doc FOREIGN KEY (doc_id) REFERENCES hts_document (id),
    CONSTRAINT hts_fk_htsidkey2idkey FOREIGN KEY (idkey_id) REFERENCES hts_idkey (id),
    CONSTRAINT hts_chk_doc2idkey UNIQUE (doc_id, idkey_id, valid_from, valid_to)
);

INSERT INTO hts_doc2idkey (doc_id, idkey_id, valid_from, valid_to)
    SELECT doc_id, idkey_id, valid_from, valid_to
    FROM hts_doc2idkey_old;

DROP TABLE hts_doc2idkey_old;

COMMIT;
