CREATE SEQUENCE quoted_object_id_seq START 1;

CREATE TABLE quoted_object (
	id INTEGER NOT NULL DEFAULT nextval('quoted_object_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE quoted_object_id_seq OWNED BY quoted_object.id;

ALTER TABLE quoted_object ADD CONSTRAINT pk_quoted_object PRIMARY KEY (id);

ALTER TABLE quoted_object ADD CONSTRAINT uc_quoted_object UNIQUE (name);

CREATE UNIQUE INDEX idx_quoted_object_name ON quoted_object(name);

CREATE SEQUENCE data_source_id_seq START 1;

CREATE TABLE data_source (
	id INTEGER NOT NULL DEFAULT nextval('data_source_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE data_source_id_seq OWNED BY data_source.id;

ALTER TABLE data_source ADD CONSTRAINT pk_data_source PRIMARY KEY (id);

ALTER TABLE data_source ADD CONSTRAINT uc_data_source UNIQUE (name);

CREATE UNIQUE INDEX idx_data_source_name ON data_source(name);

CREATE SEQUENCE data_provider_id_seq START 1;

CREATE TABLE data_provider (
	id INTEGER NOT NULL DEFAULT nextval('data_provider_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE data_provider_id_seq OWNED BY data_provider.id;

ALTER TABLE data_provider ADD CONSTRAINT pk_data_provider PRIMARY KEY (id);

ALTER TABLE data_provider ADD CONSTRAINT uc_data_provider UNIQUE (name);

CREATE UNIQUE INDEX idx_data_provider_name ON data_provider(name);

CREATE SEQUENCE data_field_id_seq START 1;

CREATE TABLE data_field (
	id INTEGER NOT NULL DEFAULT nextval('data_field_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE data_field_id_seq OWNED BY data_field.id;

ALTER TABLE data_field ADD CONSTRAINT pk_data_field PRIMARY KEY (id);

ALTER TABLE data_field ADD CONSTRAINT uc_data_field UNIQUE (name);

CREATE UNIQUE INDEX idx_data_field_name ON data_field(name);

CREATE SEQUENCE observation_time_id_seq START 1;

CREATE TABLE observation_time (
	id INTEGER NOT NULL DEFAULT nextval('observation_time_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE observation_time_id_seq OWNED BY observation_time.id;

ALTER TABLE observation_time ADD CONSTRAINT pk_observation_time PRIMARY KEY (id);

ALTER TABLE observation_time ADD CONSTRAINT uc_observation_time UNIQUE (name);

CREATE UNIQUE INDEX idx_observation_time_name ON observation_time (name);

CREATE SEQUENCE time_series_key_id_seq START 1;

CREATE TABLE time_series_key (
	id INTEGER NOT NULL DEFAULT nextval('time_series_key_id_seq'),
	qouted_obj_id INTEGER NOT NULL,
	data_soure_id INTEGER NOT NULL,   
	data_provider_id INTEGER NOT NULL,
	data_field_id INTEGER NOT NULL,
	observation_time_id INTEGER NOT NULL
);

ALTER SEQUENCE time_series_key_id_seq OWNED BY time_series_key.id;

ALTER TABLE time_series_key ADD CONSTRAINT pk_time_series_key PRIMARY KEY (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_qouted_obj_id FOREIGN KEY (qouted_obj_id) REFERENCES quoted_object (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_soure_id FOREIGN KEY (data_soure_id) REFERENCES data_source (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_provider_id FOREIGN KEY (data_provider_id) REFERENCES data_provider (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_field_id FOREIGN KEY (data_field_id) REFERENCES data_field (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_observation_time_id FOREIGN KEY (observation_time_id) REFERENCES observation_time (id);

CREATE UNIQUE INDEX idx_time_series_key ON time_series_key (data_soure_id, data_provider_id, data_field_id, observation_time_id);

CREATE TABLE time_series_data (
	id INTEGER NOT NULL,
	ts_date date NOT NULL,
	value DOUBLE PRECISION NOT NULL
);

ALTER TABLE time_series_data ADD CONSTRAINT fk_tsd_id FOREIGN KEY (id) REFERENCES time_series_key (id);

ALTER TABLE time_series_data ADD CONSTRAINT uc_time_series_data UNIQUE (id, ts_date);

CREATE SEQUENCE domain_id_seq START 1;

CREATE TABLE domain (
	id INTEGER NOT NULL DEFAULT nextval('domain_id_seq'),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER SEQUENCE domain_id_seq OWNED BY domain.id;

ALTER TABLE domain ADD CONSTRAINT pk_domain PRIMARY KEY (id);

ALTER TABLE domain ADD CONSTRAINT uc_domain UNIQUE (name);

CREATE UNIQUE INDEX idx_domain_name ON domain(name);

CREATE SEQUENCE domain_spec_identifier_id_seq START 1;
		
CREATE TABLE domain_spec_identifier (
	id INTEGER NOT NULL DEFAULT nextval('domain_spec_identifier_id_seq'),
	quoted_obj_id INTEGER NOT NULL,
	domain_id INTEGER NOT NULL,
	identifier VARCHAR(255) NOT NULL
);

ALTER SEQUENCE domain_spec_identifier_id_seq OWNED BY domain_spec_identifier.id;

ALTER TABLE domain_spec_identifier ADD CONSTRAINT pk_domain_spec_identifier PRIMARY KEY (id);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT uc_domain_spec_identifier UNIQUE (domain_id, identifier);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT fk_dsi_quoted_obj_id FOREIGN KEY (quoted_obj_id) REFERENCES quoted_object (id);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT fk_dsi_domain_id FOREIGN KEY (domain_id) REFERENCES domain (id);

CREATE UNIQUE INDEX idx_dsi_identifier ON domain_spec_identifier(identifier);

CREATE UNIQUE INDEX idx_dsi_domain_identifier ON domain_spec_identifier(domain_id, identifier);