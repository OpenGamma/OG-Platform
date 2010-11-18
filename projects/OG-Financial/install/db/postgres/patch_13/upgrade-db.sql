
create table rsk_function_unique_id (
	id int not null,
	unique_id varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_function_unique_id unique (unique_id)
);

insert into rsk_function_unique_id values (0, 'unknown');

alter table rsk_value add column function_unique_id int not null default 0;
alter table rsk_value alter column function_unique_id drop default;
alter table rsk_value add constraint rsk_fk_value2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id);
alter table rsk_failure add column function_unique_id int not null;
alter table rsk_failure add constraint rsk_fk_failure2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id);

alter table rsk_opengamma_version drop constraint rsk_chk_uq_opengamma_version;
alter table rsk_opengamma_version drop column hash;
alter table rsk_opengamma_version add constraint rsk_chk_uq_opengamma_version unique (version);

alter table rsk_run drop column run_reason;
alter table rsk_run drop column view_oid;
alter table rsk_run drop column view_version;

alter table rsk_run add constraint rsk_chk_uq_run unique (run_time_id);

alter table rsk_run drop column valuation_time;

drop view vw_rsk;
drop view vw_rsk_failure;
drop view vw_rsk_failure_reason;

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
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;
