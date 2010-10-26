create view vw_rsk as
select
rsk_computation_target_type.name as comp_target_type,
rsk_computation_target.id_scheme as comp_target_id_scheme,
rsk_computation_target.id_value as comp_target_id_value,
rsk_run.id as rsk_run_id,
rsk_observation_datetime.date_part as run_date,
rsk_observation_time.label as run_time,
rsk_calculation_configuration.name as calc_conf_name,
rsk_value_name.name,
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
rsk_observation_time
where
rsk_value.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_value.value_name_id = rsk_value_name.id and
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
rsk_run.id as rsk_run_id,
rsk_observation_datetime.date_part as run_date,
rsk_observation_time.label as run_time,
rsk_calculation_configuration.name as calc_conf_name,
rsk_value_name.name,
rsk_failure.eval_instant
from 
rsk_failure, 
rsk_calculation_configuration,
rsk_value_name,
rsk_computation_target,
rsk_computation_target_type,
rsk_run,
rsk_compute_node,
rsk_observation_datetime,
rsk_observation_time
where
rsk_failure.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_failure.value_name_id = rsk_value_name.id and
rsk_failure.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_failure.run_id = rsk_run.id and
rsk_failure.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id