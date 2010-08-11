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

DROP SEQUENCE IF EXISTS data_field_id_seq CASCADE;
DROP SEQUENCE IF EXISTS data_provider_id_seq CASCADE;
DROP SEQUENCE IF EXISTS data_source_id_seq CASCADE;
DROP SEQUENCE IF EXISTS domain_id_seq CASCADE;
DROP SEQUENCE IF EXISTS domain_spec_identifier_id_seq CASCADE;
DROP SEQUENCE IF EXISTS observation_time_id_seq CASCADE;
DROP SEQUENCE IF EXISTS quoted_object_id_seq CASCADE;
DROP SEQUENCE IF EXISTS time_series_key_id_seq CASCADE;

CREATE SEQUENCE data_field_id_seq START 1;
CREATE SEQUENCE data_provider_id_seq START 1;
CREATE SEQUENCE data_source_id_seq START 1;
CREATE SEQUENCE domain_id_seq START 1;
CREATE SEQUENCE domain_spec_identifier_id_seq START 1;
CREATE SEQUENCE observation_time_id_seq START 1;
CREATE SEQUENCE quoted_object_id_seq START 1;
CREATE SEQUENCE time_series_key_id_seq START 1;

CREATE TABLE tss_data_source (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_source_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE data_source_id_seq OWNED BY tss_data_source.id;
CREATE UNIQUE INDEX idx_data_source_name on tss_data_source(name);

CREATE TABLE tss_data_provider (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_provider_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE data_provider_id_seq OWNED BY tss_data_provider.id;
CREATE UNIQUE INDEX idx_data_provider_name on tss_data_provider(name);

CREATE TABLE tss_data_field (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('data_field_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE data_field_id_seq OWNED BY tss_data_field.id;
CREATE UNIQUE INDEX idx_data_field_name on tss_data_field(name);

CREATE TABLE tss_observation_time (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('observation_time_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE observation_time_id_seq OWNED BY tss_observation_time.id;
CREATE UNIQUE INDEX idx_observation_time_name on tss_observation_time(name);

CREATE TABLE tss_domain (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('domain_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE domain_id_seq OWNED BY tss_domain.id;
CREATE UNIQUE INDEX idx_domain_name on tss_domain(name);

CREATE TABLE tss_quoted_object (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('quoted_object_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE quoted_object_id_seq OWNED BY tss_quoted_object.id;
CREATE UNIQUE INDEX idx_quoted_object_name on tss_quoted_object(name);

CREATE TABLE tss_time_series_key (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('time_series_key_id_seq'),
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
ALTER SEQUENCE time_series_key_id_seq OWNED BY tss_time_series_key.id;
CREATE INDEX idx_time_series_key ON tss_time_series_key (data_source_id, data_provider_id, data_field_id, observation_time_id);

CREATE TABLE tss_time_series_data (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_time_series  REFERENCES tss_time_series_key (id),
	ts_date date NOT NULL,
	value DOUBLE PRECISION NOT NULL,
	PRIMARY KEY (time_series_id, ts_date)
);
CREATE UNIQUE INDEX idx_tsdata_id_date ON tss_time_series_data (time_series_id, ts_date);

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

CREATE TABLE tss_domain_spec_identifier (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('domain_spec_identifier_id_seq'),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_dsi_quoted_object  REFERENCES tss_quoted_object(id),
	domain_id INTEGER NOT NULL
	  constraint fk_dsi_domain  REFERENCES tss_domain(id),
	identifier VARCHAR(255) NOT NULL
);
ALTER SEQUENCE domain_spec_identifier_id_seq OWNED BY tss_domain_spec_identifier.id;
CREATE UNIQUE INDEX idx_dsi_domain_identifier on tss_domain_spec_identifier (domain_id, identifier);
CREATE INDEX idx_dsi_identifier ON tss_domain_spec_identifier(identifier);