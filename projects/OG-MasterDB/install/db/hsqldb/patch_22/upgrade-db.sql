DROP VIEW vw_rsk;
DROP VIEW vw_rsk_failure;

ALTER TABLE rsk_run ALTER COLUMN create_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE rsk_run ALTER COLUMN start_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE rsk_run ALTER COLUMN end_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE rsk_failure ALTER COLUMN eval_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE rsk_value ALTER COLUMN eval_instant SET DATA TYPE timestamp with time zone;

ALTER TABLE cfg_config DROP CONSTRAINT cfg_chk_config_ver_order;
ALTER TABLE cfg_config DROP CONSTRAINT cfg_chk_config_corr_order;
ALTER TABLE cfg_config ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE cfg_config ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE cfg_config ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE cfg_config ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE cfg_config ADD CONSTRAINT cfg_chk_config_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE cfg_config ADD CONSTRAINT cfg_chk_config_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE eng_functioncosts ALTER COLUMN version_instant SET DATA TYPE timestamp with time zone;

ALTER TABLE hts_document DROP CONSTRAINT hts_chk_doc_ver_order;
ALTER TABLE hts_document DROP CONSTRAINT hts_chk_doc_corr_order;
ALTER TABLE hts_document ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hts_document ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hts_document ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hts_document ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hts_document ADD CONSTRAINT hts_chk_doc_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE hts_document ADD CONSTRAINT hts_chk_doc_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE hts_point ALTER COLUMN ver_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hts_point ALTER COLUMN corr_instant SET DATA TYPE timestamp with time zone;

ALTER TABLE snp_snapshot DROP CONSTRAINT snp_chk_snapshot_ver_order;
ALTER TABLE snp_snapshot DROP CONSTRAINT snp_chk_snapshot_corr_order;
ALTER TABLE snp_snapshot ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE snp_snapshot ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE snp_snapshot ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE snp_snapshot ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE snp_snapshot ADD CONSTRAINT snp_chk_snapshot_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE snp_snapshot ADD CONSTRAINT snp_chk_snapshot_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE prt_portfolio DROP CONSTRAINT prt_chk_port_ver_order;
ALTER TABLE prt_portfolio DROP CONSTRAINT prt_chk_port_corr_order;
ALTER TABLE prt_portfolio ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE prt_portfolio ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE prt_portfolio ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE prt_portfolio ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE prt_portfolio ADD CONSTRAINT prt_chk_port_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE prt_portfolio ADD CONSTRAINT prt_chk_port_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE pos_position DROP CONSTRAINT pos_chk_posi_ver_order;
ALTER TABLE pos_position DROP CONSTRAINT pos_chk_posi_corr_order;
ALTER TABLE pos_position ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE pos_position ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE pos_position ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE pos_position ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE pos_position ADD CONSTRAINT pos_chk_posi_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE pos_position ADD CONSTRAINT pos_chk_posi_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE hol_holiday DROP CONSTRAINT hol_chk_holiday_ver_order;
ALTER TABLE hol_holiday DROP CONSTRAINT hol_chk_holiday_corr_order;
ALTER TABLE hol_holiday ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hol_holiday ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hol_holiday ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hol_holiday ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE hol_holiday ADD CONSTRAINT hol_chk_holiday_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE hol_holiday ADD CONSTRAINT hol_chk_holiday_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE exg_exchange DROP CONSTRAINT exg_chk_exchange_ver_order;
ALTER TABLE exg_exchange DROP CONSTRAINT exg_chk_exchange_corr_order;
ALTER TABLE exg_exchange ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE exg_exchange ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE exg_exchange ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE exg_exchange ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE exg_exchange ADD CONSTRAINT exg_chk_exchange_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE exg_exchange ADD CONSTRAINT exg_chk_exchange_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE sec_security DROP CONSTRAINT sec_chk_sec_ver_order;
ALTER TABLE sec_security DROP CONSTRAINT sec_chk_sec_corr_order;
ALTER TABLE sec_security ALTER COLUMN ver_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_security ALTER COLUMN ver_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_security ALTER COLUMN corr_from_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_security ALTER COLUMN corr_to_instant SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_security ADD CONSTRAINT sec_chk_sec_ver_order CHECK (ver_from_instant <= ver_to_instant);
ALTER TABLE sec_security ADD CONSTRAINT sec_chk_sec_corr_order CHECK (corr_from_instant <= corr_to_instant);

ALTER TABLE sec_equityindexoption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_equityoption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fxoption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fxoption ALTER COLUMN settlement_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_swaption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_irfutureoption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fxbarrieroption ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fxbarrieroption ALTER COLUMN settlement_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_bond ALTER COLUMN maturity_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_bond ALTER COLUMN announcement_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_bond ALTER COLUMN interestaccrual_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_bond ALTER COLUMN settlement_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_bond ALTER COLUMN firstcoupon_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_future ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_future ALTER COLUMN expiry_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_future ALTER COLUMN bondFutureFirstDeliveryDate SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_future ALTER COLUMN bondFutureLastDeliveryDate SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_futurebundle ALTER COLUMN startDate SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_futurebundle ALTER COLUMN endDate SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_cash ALTER COLUMN maturity_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fra ALTER COLUMN start_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fra ALTER COLUMN end_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_swap ALTER COLUMN trade_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_swap ALTER COLUMN effective_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_swap ALTER COLUMN maturity_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_swap ALTER COLUMN forwardstart_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_fxforward ALTER COLUMN forward_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_capfloor ALTER COLUMN maturity_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_capfloor ALTER COLUMN start_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_capfloorcmsspread ALTER COLUMN maturity_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_capfloorcmsspread ALTER COLUMN start_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_equity_variance_swap ALTER COLUMN first_observation_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_equity_variance_swap ALTER COLUMN last_observation_date SET DATA TYPE timestamp with time zone;
ALTER TABLE sec_equity_variance_swap ALTER COLUMN settlement_date SET DATA TYPE timestamp with time zone;

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
