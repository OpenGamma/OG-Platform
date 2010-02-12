CREATE TABLE quoted_object (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE quoted_object ADD CONSTRAINT pk_quoted_object PRIMARY KEY (id);

ALTER TABLE quoted_object ADD CONSTRAINT uc_quoted_object UNIQUE (name);

CREATE INDEX idx_quoted_object_name ON quoted_object(name);

CREATE TABLE data_source (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE data_source ADD CONSTRAINT pk_data_source PRIMARY KEY (id);

ALTER TABLE data_source ADD CONSTRAINT uc_data_source UNIQUE (name);

CREATE INDEX idx_data_source_name ON data_source(name);

CREATE TABLE data_provider (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE data_provider ADD CONSTRAINT pk_data_provider PRIMARY KEY (id);

ALTER TABLE data_provider ADD CONSTRAINT uc_data_provider UNIQUE (name);

CREATE INDEX idx_data_provider_name ON data_provider(name);

CREATE TABLE data_field (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE data_field ADD CONSTRAINT pk_data_field PRIMARY KEY (id);

ALTER TABLE data_field ADD CONSTRAINT uc_data_field UNIQUE (name);

CREATE INDEX idx_data_field_name ON data_field(name);

CREATE TABLE observatiON_time (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE observatiON_time ADD CONSTRAINT pk_observatiON_time PRIMARY KEY (id);

ALTER TABLE observatiON_time ADD CONSTRAINT uc_observatiON_time UNIQUE (name);

CREATE INDEX idx_observatiON_time_name ON observatiON_time (name);

CREATE TABLE time_series_key (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	qouted_obj_id INTEGER NOT NULL,
	data_soure_id INTEGER NOT NULL,   
	data_provider_id INTEGER NOT NULL,
	data_field_id INTEGER NOT NULL,
	observatiON_time_id INTEGER NOT NULL
);

ALTER TABLE time_series_key ADD CONSTRAINT pk_time_series_key PRIMARY KEY (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_qouted_obj_id FOREIGN KEY (qouted_obj_id) REFERENCES quoted_object (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_soure_id FOREIGN KEY (data_soure_id) REFERENCES data_source (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_provider_id FOREIGN KEY (data_provider_id) REFERENCES data_provider (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_data_field_id FOREIGN KEY (data_field_id) REFERENCES data_field (id);

ALTER TABLE time_series_key ADD CONSTRAINT fk_tsk_observatiON_time_id FOREIGN KEY (observatiON_time_id) REFERENCES observatiON_time (id);

CREATE INDEX idx_time_series_key ON time_series_key (data_soure_id, data_provider_id, data_field_id, observatiON_time_id);

CREATE TABLE time_series_data (
	id INTEGER NOT NULL,
	ts_date date NOT NULL,
	value DOUBLE NOT NULL
);

ALTER TABLE time_series_data ADD CONSTRAINT fk_tsd_id FOREIGN KEY (id) REFERENCES time_series_key (id);

ALTER TABLE time_series_data ADD CONSTRAINT uc_time_series_data UNIQUE (id, ts_date);

CREATE TABLE domain (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

ALTER TABLE domain ADD CONSTRAINT pk_domain PRIMARY KEY (id);

ALTER TABLE domain ADD CONSTRAINT uc_domain UNIQUE (name);

CREATE INDEX idx_domain_name ON domain(name);
		
CREATE TABLE domain_spec_identifier (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	quoted_obj_id INTEGER NOT NULL,
	domain_id INTEGER NOT NULL,
	identifier VARCHAR(255) NOT NULL
);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT pk_domain_spec_identifier PRIMARY KEY (id);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT uc_domain_spec_identifier UNIQUE (domain_id, identifier);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT fk_dsi_quoted_obj_id FOREIGN KEY (quoted_obj_id) REFERENCES quoted_object (id);

ALTER TABLE domain_spec_identifier ADD CONSTRAINT fk_dsi_domain_id FOREIGN KEY (domain_id) REFERENCES domain (id);

CREATE INDEX idx_dsi_identifier ON domain_spec_identifier(identifier);

CREATE INDEX idx_dsi_domain_identifier ON domain_spec_identifier(domain_id, identifier);