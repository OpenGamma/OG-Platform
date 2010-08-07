alter table tss_domain_spec_identifier 
	alter column id type bigint,
	alter column quoted_obj_id type bigint,
	alter column domain_id type bigint;
alter table tss_domain_spec_identifier rename column domain_id TO identification_scheme_id;
alter table tss_domain_spec_identifier rename column identifier TO identifier_value;
alter table tss_domain_spec_identifier rename column quoted_obj_id TO bundle_id;
alter table tss_domain_spec_identifier rename to tss_identifier;

alter table tss_domain
	alter column id type bigint;
alter table tss_domain rename to tss_identification_scheme;
drop index idx_domain_name;
create unique index idx_identification_scheme_name on tss_identification_scheme(name);

alter table tss_time_series_data
	alter column time_series_id type bigint;
alter table tss_time_series_data rename column time_series_id TO meta_data_id;
alter table tss_time_series_data rename to tss_data_point;

alter table tss_time_series_data_delta 
	alter column time_series_id type bigint;
alter table tss_time_series_data_delta rename column time_series_id TO meta_data_id;
alter table tss_time_series_data_delta rename to tss_data_point_delta;

alter table tss_time_series_key
	alter column id type bigint,
	alter column quoted_obj_id type bigint,
	alter column data_provider_id type bigint,
	alter column data_field_id type bigint,
	alter column observation_time_id type bigint;
alter table tss_time_series_key rename column quoted_obj_id TO bundle_id;
ALTER TABLE tss_time_series_key ADD COLUMN active INTEGER CHECK (active in (0,1));
drop INDEX idx_time_series_key;
alter table tss_time_series_key rename to tss_meta_data;
CREATE INDEX idx_meta_data ON tss_meta_data (active, data_source_id, data_provider_id, data_field_id, observation_time_id);

alter table tss_quoted_object 
	alter column id type bigint;
alter table tss_quoted_object rename to tss_identifier_bundle;	
drop index idx_quoted_object_name;
CREATE UNIQUE INDEX idx_identifier_bundle_name on tss_identifier_bundle(name);

alter table tss_data_source
	alter column id type bigint;
	
alter table tss_data_provider
	alter column id type bigint;

alter table tss_data_field
	alter column id type bigint;

alter table tss_observation_time
	alter column id type bigint;

alter SEQUENCE data_field_id_seq rename TO tss_data_field_id_seq;
alter SEQUENCE data_provider_id_seq rename TO tss_data_provider_id_seq;
alter SEQUENCE data_source_id_seq rename TO tss_data_source_id_seq;
alter SEQUENCE domain_id_seq rename TO tss_identification_scheme_id_seq;
alter SEQUENCE domain_spec_identifier_id_seq rename TO tss_identifier_id_seq;
alter SEQUENCE observation_time_id_seq rename TO tss_observation_time_id_seq;
alter SEQUENCE quoted_object_id_seq rename TO tss_quoted_object_id_seq;
alter SEQUENCE time_series_key_id_seq rename TO tss_time_series_key_id_seq;