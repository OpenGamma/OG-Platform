
DROP SEQUENCE IF EXISTS pos_master_seq;
DROP TABLE IF EXISTS pos_securitykey;
DROP TABLE IF EXISTS pos_position;
DROP TABLE IF EXISTS pos_node;
DROP TABLE IF EXISTS pos_portfolio;

DROP SEQUENCE IF EXISTS cfg_config_seq;
DROP TABLE IF EXISTS cfg_config;

DROP SEQUENCE IF EXISTS sec_security_seq;
DROP SEQUENCE IF EXISTS sec_idkey_seq;
DROP TABLE IF EXISTS sec_swap;
DROP TABLE IF EXISTS sec_fra;
DROP TABLE IF EXISTS sec_cash;
DROP TABLE IF EXISTS sec_futurebundleidentifier;
DROP TABLE IF EXISTS sec_futurebundle;
DROP TABLE IF EXISTS sec_future;
DROP TABLE IF EXISTS sec_bond;
DROP TABLE IF EXISTS sec_coupontype;
DROP TABLE IF EXISTS sec_guaranteetype;
DROP TABLE IF EXISTS sec_yieldconvention;
DROP TABLE IF EXISTS sec_market;
DROP TABLE IF EXISTS sec_issuertype;
DROP TABLE IF EXISTS sec_businessdayconvention;
DROP TABLE IF EXISTS sec_daycount;
DROP TABLE IF EXISTS sec_frequency;
DROP TABLE IF EXISTS sec_option;
DROP TABLE IF EXISTS sec_equity;
DROP TABLE IF EXISTS sec_gics;
DROP TABLE IF EXISTS sec_exchange;
DROP TABLE IF EXISTS sec_unit;
DROP TABLE IF EXISTS sec_cashrate;
DROP TABLE IF EXISTS sec_bondfuturetype;
DROP TABLE IF EXISTS sec_commodityfuturetype;
DROP TABLE IF EXISTS sec_currency;
DROP TABLE IF EXISTS sec_security2idkey;
DROP TABLE IF EXISTS sec_idkey;
DROP TABLE IF EXISTS sec_security;

DROP TABLE IF EXISTS tss_identifier CASCADE;
DROP TABLE IF EXISTS tss_identification_scheme CASCADE;
DROP TABLE IF EXISTS tss_data_point CASCADE;
DROP TABLE IF EXISTS tss_data_point_delta CASCADE;
DROP TABLE IF EXISTS tss_intraday_data_point CASCADE;
DROP TABLE IF EXISTS tss_intraday_data_point_delta CASCADE;
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

alter table rsk_computation_target add column id_version varchar(255) null;
alter table rsk_computation_target add column name varchar(255) null;
alter table rsk_computation_target drop constraint rsk_chk_uq_computation_target;
alter table rsk_computation_target add constraint rsk_chk_uq_computation_target unique (type_id, id_scheme, id_value, id_version);

drop view vw_rsk;
drop view vw_rsk_failure;

create view vw_rsk as
select
rsk_computation_target_type.name as comp_target_type,
rsk_computation_target.id_scheme as comp_target_id_scheme,
rsk_computation_target.id_value as comp_target_id_value,
rsk_computation_target.id_version as comp_target_id_version,
rsk_computation_target.name as comp_target_name,
rsk_run.id as rsk_run_id,
rsk_observation_datetime.date_part as run_date,
rsk_observation_time.label as run_time,
rsk_calculation_configuration.name as calc_conf_name,
rsk_value_name.name,
rsk_function_unique_id.unique_id as function_unique_id,
rsk_value.value, 
rsk_value.eval_instant
from 
rsk_value, 
rsk_calculation_configuration,
rsk_value_name,
rsk_computation_target,
rsk_computation_target_type,
rsk_run,
rsk_compute_node,
rsk_observation_datetime,
rsk_observation_time,
rsk_function_unique_id
where
rsk_value.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_value.value_name_id = rsk_value_name.id and
rsk_value.function_unique_id = rsk_function_unique_id.id and
rsk_value.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_value.run_id = rsk_run.id and
rsk_value.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id;

create view vw_rsk_failure as
select
rsk_computation_target_type.name as comp_target_type,
rsk_computation_target.id_scheme as comp_target_id_scheme,
rsk_computation_target.id_value as comp_target_id_value,
rsk_computation_target.id_version as comp_target_id_version,
rsk_computation_target.name as comp_target_name,
rsk_run.id as rsk_run_id,
rsk_observation_datetime.date_part as run_date,
rsk_observation_time.label as run_time,
rsk_calculation_configuration.name as calc_conf_name,
rsk_value_name.name,
rsk_function_unique_id.unique_id as function_unique_id,
rsk_failure.eval_instant,
rsk_compute_failure.function_id as failed_function,
rsk_compute_failure.exception_class,
rsk_compute_failure.exception_msg,
rsk_compute_failure.stack_trace 
from 
rsk_failure, 
rsk_calculation_configuration,
rsk_value_name,
rsk_computation_target,
rsk_computation_target_type,
rsk_run,
rsk_compute_node,
rsk_observation_datetime,
rsk_observation_time,
rsk_function_unique_id,
rsk_failure_reason,
rsk_compute_failure
where
rsk_failure.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_failure.value_name_id = rsk_value_name.id and
rsk_failure.function_unique_id = rsk_function_unique_id.id and
rsk_failure.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_failure.run_id = rsk_run.id and
rsk_failure.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id and
rsk_failure_reason.rsk_failure_id = rsk_failure.id and
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;

