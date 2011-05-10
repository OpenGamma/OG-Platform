DROP TABLE IF EXISTS tss_identifier CASCADE;
DROP TABLE IF EXISTS tss_identification_scheme CASCADE;
DROP TABLE IF EXISTS tss_data_point CASCADE;
DROP TABLE IF EXISTS tss_data_point_delta CASCADE;
DROP TABLE IF EXISTS tss_meta_data CASCADE;
DROP TABLE IF EXISTS tss_identifier_bundle CASCADE;
DROP TABLE IF EXISTS tss_data_source CASCADE;
DROP TABLE IF EXISTS tss_data_provider CASCADE;
DROP TABLE IF EXISTS tss_data_field CASCADE;
DROP TABLE IF EXISTS tss_observation_time CASCADE;

DROP SEQUENCE IF EXISTS tss_data_field_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_data_provider_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_data_source_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_identification_scheme_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_identifier_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_observation_time_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_identifier_bundle_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tss_meta_data_id_seq CASCADE;


CREATE SEQUENCE tss_data_field_id_seq START 1;
CREATE SEQUENCE tss_data_provider_id_seq START 1;
CREATE SEQUENCE tss_data_source_id_seq START 1;
CREATE SEQUENCE tss_identification_scheme_id_seq START 1;
CREATE SEQUENCE tss_identifier_id_seq START 1;
CREATE SEQUENCE tss_observation_time_id_seq START 1;
CREATE SEQUENCE tss_identifier_bundle_id_seq START 1;
CREATE SEQUENCE tss_meta_data_id_seq START 1;

CREATE TABLE tss_data_source (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_source_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_data_source_id_seq OWNED BY tss_data_source.id;
CREATE UNIQUE INDEX idx_data_source_name on tss_data_source(name);

CREATE TABLE tss_data_provider (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_provider_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_data_provider_id_seq OWNED BY tss_data_provider.id;
CREATE UNIQUE INDEX idx_data_provider_name on tss_data_provider(name);

CREATE TABLE tss_data_field (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_data_field_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_data_field_id_seq OWNED BY tss_data_field.id;
CREATE UNIQUE INDEX idx_data_field_name on tss_data_field(name);

CREATE TABLE tss_observation_time (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_observation_time_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_observation_time_id_seq OWNED BY tss_observation_time.id;
CREATE UNIQUE INDEX idx_observation_time_name on tss_observation_time(name);

CREATE TABLE tss_identification_scheme (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_identification_scheme_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_identification_scheme_id_seq OWNED BY tss_identification_scheme.id;
CREATE UNIQUE INDEX idx_identification_scheme_name on tss_identification_scheme(name);

CREATE TABLE tss_identifier_bundle (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_identifier_bundle_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);
ALTER SEQUENCE tss_identifier_bundle_id_seq OWNED BY tss_identifier_bundle.id;
CREATE UNIQUE INDEX idx_identifier_bundle_name on tss_identifier_bundle(name);

CREATE TABLE tss_meta_data (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_meta_data_id_seq'),
	active INTEGER NOT NULL
	  CONSTRAINT active_constraint CHECK (active IN (0,1)),
	bundle_id BIGINT NOT NULL
	  constraint fk_meta_bundle  REFERENCES tss_identifier_bundle(id),
	data_source_id BIGINT NOT NULL
	  constraint fk_meta_data_source  REFERENCES tss_data_source(id),
	data_provider_id BIGINT NOT NULL
	  constraint fk_meta_data_provider  REFERENCES tss_data_provider(id),
	data_field_id BIGINT NOT NULL
	  constraint fk_meta_data_field  REFERENCES tss_data_field(id),
	observation_time_id BIGINT NOT NULL
	  constraint fk_meta_observation_time  REFERENCES tss_observation_time(id)
);
ALTER SEQUENCE tss_meta_data_id_seq OWNED BY tss_meta_data.id;
CREATE INDEX idx_meta_data ON tss_meta_data (active, data_source_id, data_provider_id, data_field_id, observation_time_id);

CREATE TABLE tss_data_point (
	meta_data_id BIGINT NOT NULL
	  constraint fk_dp_meta_data  REFERENCES tss_meta_data (id),
	ts_date date NOT NULL,
	value DOUBLE PRECISION NOT NULL,
	PRIMARY KEY (meta_data_id, ts_date)
);

CREATE TABLE tss_data_point_delta (
	meta_data_id BIGINT NOT NULL
	  constraint fk_dp_delta_meta_data  REFERENCES tss_meta_data (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date date NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint CHECK ( operation IN ('I', 'U', 'D', 'Q'))
);

CREATE TABLE tss_intraday_data_point (
	meta_data_id BIGINT NOT NULL
	  constraint fk_i_dp_meta_data  REFERENCES tss_meta_data (id),
	ts_date TIMESTAMP NOT NULL,
	value DOUBLE PRECISION NOT NULL,
	PRIMARY KEY (meta_data_id, ts_date)
);

CREATE TABLE tss_intraday_data_point_delta (
	meta_data_id BIGINT NOT NULL
	  constraint fk_i_dp_delta_meta_data  REFERENCES tss_meta_data (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date TIMESTAMP NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint_i CHECK ( operation IN ('I', 'U', 'D', 'Q'))
);

CREATE TABLE tss_identifier (
	id BIGINT NOT NULL
	  PRIMARY KEY
	  DEFAULT nextval('tss_identifier_id_seq'),
	bundle_id BIGINT NOT NULL
	  constraint fk_identifier_bundle  REFERENCES tss_identifier_bundle(id),
	identification_scheme_id BIGINT NOT NULL
	  constraint fk_identifier_identification_scheme  REFERENCES tss_identification_scheme(id),
	identifier_value VARCHAR(255) NOT NULL,
	valid_from date,
	valid_to date,
	constraint rsk_chk_uq_identifier unique (identification_scheme_id, identifier_value, valid_from, valid_to)
);

ALTER SEQUENCE tss_identifier_id_seq OWNED BY tss_identifier.id;
CREATE INDEX idx_identifier_scheme_value on tss_identifier (identification_scheme_id, identifier_value);
CREATE INDEX idx_identifier_value ON tss_identifier(identifier_value);