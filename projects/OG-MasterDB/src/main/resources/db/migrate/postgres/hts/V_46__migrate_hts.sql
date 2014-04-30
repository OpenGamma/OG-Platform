START TRANSACTION;
	-- update the version
  UPDATE hts_schema_version SET version_value='46' WHERE version_key='schema_patch';
	
	CREATE TABLE hts_permission (
    id bigint NOT NULL DEFAULT nextval('hts_dimension_seq'),
    doc_id bigint NOT NULL,
    permission varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT hts_fk_hts_permission2doc FOREIGN KEY (doc_id) REFERENCES hts_document (id),
    CONSTRAINT hts_chk_hts_permission UNIQUE (doc_id, permission)
	);

	CREATE INDEX ix_hts_permission ON hts_permission(doc_id, permission);
COMMIT;
