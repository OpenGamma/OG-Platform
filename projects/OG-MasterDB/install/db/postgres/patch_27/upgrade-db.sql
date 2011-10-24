create table rsk_value_constraints (
    id int not null,
    synthetic_constraints varchar(1024) not null,

    primary key (id),

    constraint rsk_chk_uq_value_constraints unique (synthetic_constraints)
);



ALTER TABLE rsk_value ADD COLUMN value_constraints_id int not null;

ALTER TABLE rsk_value ADD CONSTRAINT rsk_fk_value2value_constraints FOREIGN KEY (value_constraints_id) REFERENCES rsk_value_constraints (id);

ALTER TABLE rsk_value DROP CONSTRAINT rsk_chk_uq_value;

ALTER TABLE rsk_value ADD CONSTRAINT rsk_chk_uq_value unique (calculation_configuration_id, value_name_id, value_constraints_id, computation_target_id);





ALTER TABLE rsk_failure ADD COLUMN value_constraints_id int not null;

ALTER TABLE rsk_failure ADD CONSTRAINT rsk_fk_failure2value_constraints FOREIGN KEY (value_constraints_id) REFERENCES rsk_value_constraints (id);

ALTER TABLE rsk_failure DROP CONSTRAINT rsk_chk_uq_failure;

ALTER TABLE rsk_failure ADD CONSTRAINT rsk_chk_uq_failure unique (calculation_configuration_id, value_name_id, value_constraints_id, computation_target_id);



-------------------------------------
-- Views
-------------------------------------

DROP VIEW vw_rsk;

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
rsk_value_constraints.synthetic_constraints,
rsk_function_unique_id.unique_id as function_unique_id,
rsk_value.value,
rsk_value.eval_instant
from
rsk_value,
rsk_calculation_configuration,
rsk_value_name,
rsk_value_constraints,
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
rsk_value.value_constraints_id = rsk_value_constraints.id and
rsk_value.function_unique_id = rsk_function_unique_id.id and
rsk_value.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_value.run_id = rsk_run.id and
rsk_value.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id;

DROP VIEW vw_rsk_failure;

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
rsk_value_constraints.synthetic_constraints,
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
rsk_value_constraints,
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
rsk_failure.value_constraints_id = rsk_value_constraints.id and
rsk_failure.function_unique_id = rsk_function_unique_id.id and
rsk_failure.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_failure.run_id = rsk_run.id and
rsk_failure.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id and
rsk_failure_reason.rsk_failure_id = rsk_failure.id and
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;

