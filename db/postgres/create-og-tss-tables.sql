DROP TABLE IF EXISTS domain_spec_identifier CASCADE;
DROP TABLE IF EXISTS domain CASCADE;
DROP TABLE IF EXISTS time_series_data CASCADE;
DROP TABLE IF EXISTS time_series_data_delta CASCADE;
DROP TABLE IF EXISTS time_series_key CASCADE;
DROP TABLE IF EXISTS quoted_object CASCADE;
DROP TABLE IF EXISTS data_source CASCADE;
DROP TABLE IF EXISTS data_provider CASCADE;
DROP TABLE IF EXISTS data_field CASCADE;
DROP TABLE IF EXISTS observation_time CASCADE;

CREATE SEQUENCE data_field_id_seq START 1;
CREATE SEQUENCE data_provider_id_seq START 1;
CREATE SEQUENCE data_source_id_seq START 1;
CREATE SEQUENCE domain_id_seq START 1;
CREATE SEQUENCE domain_spec_identifier_id_seq START 1;
CREATE SEQUENCE observation_time_id_seq START 1;
CREATE SEQUENCE quoted_object_id_seq START 1;
CREATE SEQUENCE time_series_key_id_seq START 1;

CREATE TABLE data_source (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_source_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE data_source_id_seq OWNED BY data_source.id;

CREATE UNIQUE INDEX idx_data_source_name ON data_source(name);

REVOKE ALL ON data_source FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE data_source TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE data_source_id_seq TO tssupdater;
GRANT SELECT ON TABLE data_source TO PUBLIC;

CREATE TABLE data_provider (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_provider_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE data_provider_id_seq OWNED BY data_provider.id;

CREATE UNIQUE INDEX idx_data_provider_name ON data_provider(name);

REVOKE ALL ON data_provider FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE data_provider TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE data_provider_id_seq TO tssupdater;
GRANT SELECT ON TABLE data_provider TO PUBLIC;

CREATE TABLE data_field (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_field_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE data_field_id_seq OWNED BY data_field.id;

CREATE UNIQUE INDEX idx_data_field_name ON data_field(name);

REVOKE ALL ON data_field FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE data_field TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE data_field_id_seq TO tssupdater;
GRANT SELECT ON TABLE data_field TO PUBLIC;

CREATE TABLE observation_time (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('observation_time_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE observation_time_id_seq OWNED BY observation_time.id;

CREATE UNIQUE INDEX idx_observation_time_name ON observation_time(name);

REVOKE ALL ON observation_time FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE observation_time TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE observation_time_id_seq TO tssupdater;
GRANT SELECT ON TABLE observation_time TO PUBLIC;

CREATE TABLE domain (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('domain_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE domain_id_seq OWNED BY domain.id;

CREATE UNIQUE INDEX idx_domain_name ON domain(name);

REVOKE ALL ON domain FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE domain TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE domain_id_seq TO tssupdater;
GRANT SELECT ON TABLE domain TO PUBLIC;

CREATE TABLE quoted_object (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('quoted_object_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE quoted_object_id_seq OWNED BY quoted_object.id;

CREATE UNIQUE INDEX idx_quoted_object_name ON quoted_object(name);

REVOKE ALL ON quoted_object FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE quoted_object TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE quoted_object_id_seq TO tssupdater;
GRANT SELECT ON TABLE quoted_object TO PUBLIC;

CREATE TABLE time_series_key (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('time_series_key_id_seq'),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_tsk_quoted_obj  REFERENCES quoted_object(id),
	data_source_id INTEGER NOT NULL
	  constraint fk_tsk_data_source  REFERENCES data_source(id),
	data_provider_id INTEGER NOT NULL
	  constraint fk_tsk_data_provider  REFERENCES data_provider(id),
	data_field_id INTEGER NOT NULL
	  constraint fk_tsk_data_field  REFERENCES data_field(id),
	observation_time_id INTEGER NOT NULL
	  constraint fk_tsk_observation_time  REFERENCES observation_time(id)
);
ALTER SEQUENCE time_series_key_id_seq OWNED BY time_series_key.id;

CREATE INDEX idx_time_series_key ON time_series_key (data_source_id, data_provider_id, data_field_id, observation_time_id);

REVOKE ALL ON time_series_key FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE time_series_key TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE time_series_key_id_seq TO tssupdater;
GRANT SELECT ON TABLE time_series_key TO PUBLIC;

CREATE TABLE time_series_data (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_time_series  REFERENCES time_series_key (id),
	ts_date date NOT NULL,
	value DOUBLE PRECISION NOT NULL
);

CREATE UNIQUE INDEX idx_tsdata_id_date ON time_series_data (time_series_id, ts_date);

REVOKE ALL ON time_series_data FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE time_series_data TO tssupdater;
GRANT SELECT ON TABLE time_series_data TO PUBLIC;

CREATE TABLE time_series_data_delta (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_delta_time_series  REFERENCES time_series_key (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date date NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint CHECK ( operation IN ('I', 'U', 'D', 'Q')),
	PRIMARY KEY (time_series_id, time_stamp, ts_date)
);

REVOKE ALL ON time_series_data_delta FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE time_series_data_delta TO tssupdater;
GRANT SELECT ON TABLE time_series_data_delta TO PUBLIC;

CREATE TABLE domain_spec_identifier (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('domain_spec_identifier_id_seq'),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_dsi_quoted_object  REFERENCES quoted_object(id),
	domain_id INTEGER NOT NULL
	  constraint fk_dsi_domain  REFERENCES domain(id),
	identifier VARCHAR(255) NOT NULL
);
ALTER SEQUENCE domain_spec_identifier_id_seq OWNED BY domain_spec_identifier.id;

CREATE UNIQUE INDEX idx_dsi_domain_identifier ON domain_spec_identifier (domain_id, identifier);

CREATE INDEX idx_dsi_identifier ON domain_spec_identifier(identifier);

REVOKE ALL ON domain_spec_identifier FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE domain_spec_identifier TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE domain_spec_identifier_id_seq TO tssupdater;
GRANT SELECT ON TABLE domain_spec_identifier TO PUBLIC;