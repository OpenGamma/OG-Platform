alter SEQUENCE data_field_id_seq rename TO tss_data_field_id_seq;
alter SEQUENCE data_provider_id_seq rename TO tss_data_provider_id_seq;
alter SEQUENCE data_source_id_seq rename TO tss_data_source_id_seq;
alter SEQUENCE domain_id_seq rename TO tss_identification_scheme_id_seq;
alter SEQUENCE domain_spec_identifier_id_seq rename TO tss_identifier_id_seq;
alter SEQUENCE observation_time_id_seq rename TO tss_observation_time_id_seq;
alter SEQUENCE quoted_object_id_seq rename TO tss_identifier_bundle_id_seq;
alter SEQUENCE time_series_key_id_seq rename TO tss_meta_data_id_seq;

alter table tss_quoted_object 
	alter column id type bigint;
alter table tss_quoted_object rename to tss_identifier_bundle;	
drop index idx_quoted_object_name;
CREATE UNIQUE INDEX idx_identifier_bundle_name on tss_identifier_bundle(name);

alter table tss_domain
	alter column id type bigint;
alter table tss_domain rename to tss_identification_scheme;
drop index idx_domain_name;
create unique index idx_identification_scheme_name on tss_identification_scheme(name);

alter table tss_domain_spec_identifier 
	alter column id type bigint,
	alter column quoted_obj_id type bigint,
	alter column domain_id type bigint;
alter table tss_domain_spec_identifier rename column domain_id TO identification_scheme_id;
alter table tss_domain_spec_identifier rename column identifier TO identifier_value;
alter table tss_domain_spec_identifier rename column quoted_obj_id TO bundle_id;
alter table tss_domain_spec_identifier DROP CONSTRAINT fk_dsi_quoted_object;
alter table tss_domain_spec_identifier DROP CONSTRAINT fk_dsi_domain;
alter table tss_domain_spec_identifier rename to tss_identifier;
drop index idx_dsi_domain_identifier;
drop index idx_dsi_identifier;
CREATE UNIQUE INDEX idx_identifier_scheme_value on tss_identifier(identification_scheme_id, identifier_value);
CREATE INDEX idx_identifier_value ON tss_identifier (identifier_value);
ALTER TABLE tss_identifier ADD CONSTRAINT fk_identifier_bundle FOREIGN KEY (bundle_id) REFERENCES tss_identifier_bundle (id);
ALTER TABLE tss_identifier ADD CONSTRAINT fk_identifier_identification_scheme FOREIGN KEY (identification_scheme_id) REFERENCES tss_identification_scheme (id);

alter table tss_time_series_key
	alter column id type bigint,
	alter column quoted_obj_id type bigint,
	alter column data_source_id type bigint,
	alter column data_provider_id type bigint,
	alter column data_field_id type bigint,
	alter column observation_time_id type bigint;
ALTER TABLE tss_time_series_key ADD COLUMN active INTEGER;
ALTER TABLE tss_time_series_key ADD CONSTRAINT active_constraint CHECK (active in (0,1));
UPDATE tss_time_series_key SET active = 1;
ALTER TABLE tss_time_series_key ALTER COLUMN active SET NOT NULL;
alter table tss_time_series_key rename column quoted_obj_id TO bundle_id;
drop INDEX idx_time_series_key;
alter table tss_time_series_key DROP CONSTRAINT fk_tsk_quoted_obj;
alter table tss_time_series_key DROP CONSTRAINT fk_tsk_data_source;
alter table tss_time_series_key DROP CONSTRAINT fk_tsk_data_provider;
alter table tss_time_series_key DROP CONSTRAINT fk_tsk_data_field;
alter table tss_time_series_key DROP CONSTRAINT fk_tsk_observation_time;
alter table tss_time_series_key rename to tss_meta_data;
CREATE INDEX idx_meta_data ON tss_meta_data (active, data_source_id, data_provider_id, data_field_id, observation_time_id);
ALTER TABLE tss_meta_data ADD CONSTRAINT fk_meta_bundle FOREIGN KEY (bundle_id) REFERENCES tss_identifier_bundle (id);
ALTER TABLE tss_meta_data ADD CONSTRAINT fk_meta_data_source FOREIGN KEY (data_source_id) REFERENCES tss_data_source (id);
ALTER TABLE tss_meta_data ADD CONSTRAINT fk_meta_data_provider FOREIGN KEY (data_provider_id) REFERENCES tss_data_provider (id);
ALTER TABLE tss_meta_data ADD CONSTRAINT fk_meta_data_field FOREIGN KEY (data_field_id) REFERENCES tss_data_field (id);
ALTER TABLE tss_meta_data ADD CONSTRAINT fk_meta_observation_time FOREIGN KEY (observation_time_id) REFERENCES tss_observation_time (id);

alter table tss_time_series_data
	alter column time_series_id type bigint;
alter table tss_time_series_data rename column time_series_id TO meta_data_id;
alter table tss_time_series_data DROP CONSTRAINT fk_tsd_time_series;
alter table tss_time_series_data rename to tss_data_point;
drop index idx_tsdata_id_date;
ALTER TABLE tss_data_point ADD CONSTRAINT fk_dp_meta_data FOREIGN KEY (meta_data_id) REFERENCES tss_meta_data (id);

alter table tss_time_series_data_delta 
	alter column time_series_id type bigint;
alter table tss_time_series_data_delta rename column time_series_id TO meta_data_id;
alter table tss_time_series_data_delta DROP CONSTRAINT fk_tsd_delta_time_series;
alter table tss_time_series_data_delta rename to tss_data_point_delta;
ALTER TABLE tss_data_point_delta ADD CONSTRAINT fk_dp_delta_meta_data FOREIGN KEY (meta_data_id) REFERENCES tss_meta_data (id);

alter table tss_data_source
	alter column id type bigint;
	
alter table tss_data_provider
	alter column id type bigint;

alter table tss_data_field
	alter column id type bigint;

alter table tss_observation_time
	alter column id type bigint;