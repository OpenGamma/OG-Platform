alter table time_series_key drop constraint fk_tsk_quoted_obj;
alter table time_series_key drop constraint fk_tsk_data_source;
alter table time_series_key drop constraint fk_tsk_data_provider;
alter table time_series_key drop constraint fk_tsk_data_field;
alter table time_series_key drop constraint fk_tsk_observation_time;
alter table time_series_data drop constraint fk_tsd_time_series;
alter table time_series_data_delta drop constraint fk_tsd_delta_time_series;
alter table time_series_data_delta drop constraint operation_constraint;
alter table domain_spec_identifier drop constraint fk_dsi_quoted_object;
alter table domain_spec_identifier drop constraint fk_dsi_domain;

rename table data_source to tss_data_source;
rename table data_provider to tss_data_provider;
rename table data_field to tss_data_field;
rename table observation_time to tss_observation_time;
rename table domain to tss_domain;
rename table quoted_object to tss_quoted_object;

rename table time_series_key to tss_time_series_key;
alter table tss_time_series_key add constraint fk_tsk_quoted_obj foreign key(quoted_obj_id) references tss_quoted_object(id);
alter table tss_time_series_key add constraint fk_tsk_data_source foreign key(data_source_id) references tss_data_source(id);
alter table tss_time_series_key add constraint fk_tsk_data_provider foreign key(data_provider_id) references tss_data_provider(id);
alter table tss_time_series_key add constraint fk_tsk_data_field foreign key(data_field_id) references tss_data_field(id);
alter table tss_time_series_key add constraint fk_tsk_observation_time foreign key(observation_time_id) references tss_observation_time(id);

rename table time_series_data to tss_time_series_data;
alter table tss_time_series_data add constraint fk_tsd_time_series foreign key(time_series_id) references tss_time_series_key(id);

rename table time_series_data_delta to tss_time_series_data_delta;
alter table tss_time_series_data_delta add constraint fk_tsd_delta_time_series foreign key(time_series_id) references tss_time_series_key(id);

rename table domain_spec_identifier to tss_domain_spec_identifier;
alter table tss_domain_spec_identifier add constraint fk_tss_dsi_quoted_object foreign key(quoted_obj_id) references tss_quoted_object(id);