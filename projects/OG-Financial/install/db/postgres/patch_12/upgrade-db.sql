create table rsk_run_status_code (
    id int not null,	 	            
    name varchar(255) not null,
    
    primary key (id),
    
    constraint rsk_chk_rsk_run_status_code check
        ((id = 0 and name = 'SUCCESS') or
         (id = 1 and name = 'FAILURE') or 
         (id = 2 and name = 'RUNNING') or
         (id = 3 and name = 'NOT_RUNNING'))
);

insert into rsk_run_status_code (id, name) values (0, 'SUCCESS');
insert into rsk_run_status_code (id, name) values (1, 'FAILURE');
insert into rsk_run_status_code (id, name) values (2, 'RUNNING');
insert into rsk_run_status_code (id, name) values (3, 'NOT_RUNNING');

alter table rsk_run_status add constraint rsk_fk_run_status2code
        foreign key (status) references rsk_run_status_code (id);

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
rsk_failure.id,
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
rsk_observation_datetime.observation_time_id = rsk_observation_time.id;

create view vw_rsk_failure_reason as
select
rsk_failure_reason.id,
rsk_failure_reason.rsk_failure_id,
rsk_compute_failure.function_id,
rsk_compute_failure.exception_class,
rsk_compute_failure.exception_msg,
rsk_compute_failure.stack_trace 
from 
rsk_failure_reason,
rsk_compute_failure
where
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;