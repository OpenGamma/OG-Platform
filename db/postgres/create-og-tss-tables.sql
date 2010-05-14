DROP TABLE IF EXISTS tss_domain_spec_identifier CASCADE;
DROP TABLE IF EXISTS tss_domain CASCADE;
DROP TABLE IF EXISTS tss_time_series_data CASCADE;
DROP TABLE IF EXISTS tss_time_series_data_delta CASCADE;
DROP TABLE IF EXISTS tss_time_series_key CASCADE;
DROP TABLE IF EXISTS tss_quoted_object CASCADE;
DROP TABLE IF EXISTS tss_data_source CASCADE;
DROP TABLE IF EXISTS tss_data_provider CASCADE;
DROP TABLE IF EXISTS tss_data_field CASCADE;
DROP TABLE IF EXISTS tss_observation_time CASCADE;

CREATE SEQUENCE tss_data_field_id_seq START 1;
CREATE SEQUENCE tss_data_provider_id_seq START 1;
CREATE SEQUENCE tss_data_source_id_seq START 1;
CREATE SEQUENCE tss_domain_id_seq START 1;
CREATE SEQUENCE tss_domain_spec_identifier_id_seq START 1;
CREATE SEQUENCE tss_observation_time_id_seq START 1;
CREATE SEQUENCE tss_quoted_object_id_seq START 1;
CREATE SEQUENCE tss_time_series_key_id_seq START 1;

CREATE TABLE tss_data_source (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_source_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE tss_data_source_id_seq OWNED BY tss_data_source.id;

CREATE UNIQUE INDEX idx_data_source_name ON tss_data_source(name);

REVOKE ALL ON tss_data_source FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_source TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_source_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_source TO PUBLIC;

CREATE TABLE tss_data_provider (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_provider_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_data_provider_id_seq OWNED BY tss_data_provider.id;

CREATE UNIQUE INDEX idx_data_provider_name ON tss_data_provider(name);

REVOKE ALL ON tss_data_provider FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_provider TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_provider_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_provider TO PUBLIC;

CREATE TABLE tss_data_field (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_field_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_data_field_id_seq OWNED BY tss_data_field.id;

CREATE UNIQUE INDEX idx_data_field_name ON tss_data_field(name);

REVOKE ALL ON tss_data_field FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_field TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_field_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_field TO PUBLIC;

CREATE TABLE tss_observation_time (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_observation_time_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_observation_time_id_seq OWNED BY tss_observation_time.id;

CREATE UNIQUE INDEX idx_observation_time_name ON tss_observation_time(name);

REVOKE ALL ON tss_observation_time FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_observation_time TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_observation_time_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_observation_time TO PUBLIC;

CREATE TABLE tss_domain (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_domain_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_domain_id_seq OWNED BY tss_domain.id;

CREATE UNIQUE INDEX idx_domain_name ON tss_domain(name);

REVOKE ALL ON tss_domain FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_domain TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_domain_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_domain TO PUBLIC;

CREATE TABLE tss_quoted_object (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_quoted_object_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_quoted_object_id_seq OWNED BY tss_quoted_object.id;

CREATE UNIQUE INDEX idx_quoted_object_name ON tss_quoted_object(name);

REVOKE ALL ON tss_quoted_object FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_quoted_object TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_quoted_object_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_quoted_object TO PUBLIC;

CREATE TABLE tss_time_series_key (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_time_series_key_id_seq'),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_tsk_quoted_obj  REFERENCES tss_quoted_object(id),
	data_source_id INTEGER NOT NULL
	  constraint fk_tsk_data_source  REFERENCES tss_data_source(id),
	data_provider_id INTEGER NOT NULL
	  constraint fk_tsk_data_provider  REFERENCES tss_data_provider(id),
	data_field_id INTEGER NOT NULL
	  constraint fk_tsk_data_field  REFERENCES tss_data_field(id),
	observation_time_id INTEGER NOT NULL
	  constraint fk_tsk_observation_time  REFERENCES tss_observation_time(id)
);
ALTER SEQUENCE tss_time_series_key_id_seq OWNED BY tss_time_series_key.id;

CREATE INDEX idx_time_series_key ON tss_time_series_key (data_source_id, data_provider_id, data_field_id, observation_time_id);

REVOKE ALL ON tss_time_series_key FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_key TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_time_series_key_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_key TO PUBLIC;

CREATE TABLE tss_time_series_data (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_time_series  REFERENCES tss_time_series_key (id),
	ts_date date NOT NULL,
	value DOUBLE PRECISION NOT NULL,
	PRIMARY KEY (time_series_id)
);

CREATE UNIQUE INDEX idx_tsdata_id_date ON tss_time_series_data (time_series_id, ts_date);

REVOKE ALL ON tss_time_series_data FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_data TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_data TO PUBLIC;

CREATE TABLE tss_time_series_data_delta (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_delta_time_series  REFERENCES tss_time_series_key (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date date NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint CHECK ( operation IN ('I', 'U', 'D', 'Q')),
	PRIMARY KEY (time_series_id, time_stamp, ts_date)
);

REVOKE ALL ON tss_time_series_data_delta FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_data_delta TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_data_delta TO PUBLIC;

CREATE TABLE tss_domain_spec_identifier (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_domain_spec_identifier_id_seq'),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_dsi_quoted_object  REFERENCES tss_quoted_object(id),
	domain_id INTEGER NOT NULL
	  constraint fk_dsi_domain  REFERENCES tss_domain(id),
	identifier VARCHAR(255) NOT NULL
);
ALTER SEQUENCE tss_domain_spec_identifier_id_seq OWNED BY tss_domain_spec_identifier.id;

CREATE UNIQUE INDEX idx_dsi_domain_identifier ON tss_domain_spec_identifier (domain_id, identifier);

CREATE INDEX idx_dsi_identifier ON tss_domain_spec_identifier(identifier);

REVOKE ALL ON tss_domain_spec_identifier FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_domain_spec_identifier TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_domain_spec_identifier_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_domain_spec_identifier TO PUBLIC;