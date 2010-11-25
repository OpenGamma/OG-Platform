
DROP SEQUENCE IF EXISTS pos_master_seq;
DROP TABLE IF EXISTS pos_securitykey;
DROP TABLE IF EXISTS pos_position;
DROP TABLE IF EXISTS pos_node;
DROP TABLE IF EXISTS pos_portfolio;

-- DROP TABLE IF EXISTS tss_identifier CASCADE;
-- DROP TABLE IF EXISTS tss_identification_scheme CASCADE;
-- DROP TABLE IF EXISTS tss_data_point CASCADE;
-- DROP TABLE IF EXISTS tss_data_point_delta CASCADE;
-- DROP TABLE IF EXISTS tss_intraday_data_point CASCADE;
-- DROP TABLE IF EXISTS tss_intraday_data_point_delta CASCADE;
-- DROP TABLE IF EXISTS tss_meta_data CASCADE;
-- DROP TABLE IF EXISTS tss_identifier_bundle CASCADE;
-- DROP TABLE IF EXISTS tss_data_source CASCADE;
-- DROP TABLE IF EXISTS tss_data_provider CASCADE;
-- DROP TABLE IF EXISTS tss_data_field CASCADE;
-- DROP TABLE IF EXISTS tss_observation_time CASCADE;

-- DROP SEQUENCE IF EXISTS tss_data_field_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_data_provider_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_data_source_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_identification_scheme_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_identifier_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_observation_time_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_identifier_bundle_id_seq CASCADE;
-- DROP SEQUENCE IF EXISTS tss_meta_data_id_seq CASCADE;

alter table rsk_computation_target add column id_version varchar(255) null;
alter table rsk_computation_target add column name varchar(255) null;
alter table rsk_computation_target drop constraint rsk_chk_uq_computation_target;
alter table rsk_computation_target add constraint rsk_chk_uq_computation_target unique (type_id, id_scheme, id_value, id_version);

