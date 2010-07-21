CREATE TABLE tss_data_source (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_data_source_name on tss_data_source(name);

CREATE TABLE tss_data_provider (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_data_provider_name on tss_data_provider(name);

CREATE TABLE tss_data_field (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_data_field_name on tss_data_field(name);

CREATE TABLE tss_observation_time (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_observation_time_name on tss_observation_time(name);

CREATE TABLE tss_domain (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_domain_name on tss_domain(name);

CREATE TABLE tss_quoted_object (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255)
);

CREATE UNIQUE INDEX idx_quoted_object_name on tss_quoted_object(name);

CREATE TABLE tss_time_series_key (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
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

CREATE INDEX idx_time_series_key ON tss_time_series_key (data_source_id, data_provider_id, data_field_id, observation_time_id);

CREATE TABLE tss_time_series_data (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_time_series  REFERENCES tss_time_series_key (id),
	ts_date date NOT NULL,
	value DOUBLE NOT NULL,
	PRIMARY KEY (time_series_id, ts_date)
);

CREATE UNIQUE INDEX idx_tsdata_id_date ON tss_time_series_data (time_series_id, ts_date); 

CREATE TABLE tss_time_series_data_delta (
	time_series_id INTEGER NOT NULL
	  constraint fk_tsd_delta_time_series  REFERENCES tss_time_series_key (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date date NOT NULL,
	old_value DOUBLE NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint CHECK ( operation IN ('I', 'U', 'D', 'Q')),
	PRIMARY KEY (time_series_id, time_stamp, ts_date)
);

CREATE UNIQUE INDEX idx_tsdata_id_stamp_date ON tss_time_series_data_delta (time_series_id, time_stamp, ts_date);

CREATE TABLE tss_domain_spec_identifier (
	id INTEGER NOT NULL
	  PRIMARY KEY
	  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	quoted_obj_id INTEGER NOT NULL
	  constraint fk_dsi_quoted_object  REFERENCES tss_quoted_object(id),
	domain_id INTEGER NOT NULL
	  constraint fk_dsi_domain  REFERENCES tss_domain(id),
	identifier VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_dsi_domain_identifier on tss_domain_spec_identifier (domain_id, identifier);

CREATE INDEX idx_dsi_identifier ON tss_domain_spec_identifier(identifier);
