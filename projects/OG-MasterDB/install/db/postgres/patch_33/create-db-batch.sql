-------------------------------------
-- Static data
-------------------------------------

create table rsk_observation_time (
    id int not null,
    label varchar(255) not null,                -- LDN_CLOSE
    
    primary key (id),
    
    constraint rsk_chk_uq_obs_time unique (label)
);

create table rsk_observation_datetime (
	id int not null,
	date_part date not null,  
	time_part time null,						-- null if time of LDN_CLOSE not fixed yet
	observation_time_id int not null,    		  
	
	primary key (id),
	
	constraint rsk_fk_obs_datetime2obs_time
	    foreign key (observation_time_id) references rsk_observation_time (id),
	    
	constraint rsk_chk_obs_datetime check 
	    (time_part is not null or observation_time_id is not null), 
	
	constraint rsk_chk_uq_obs_datetime unique (date_part, observation_time_id)
);

create table rsk_compute_host (
	id int not null,
	host_name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_compute_host unique (host_name)
);

create table rsk_compute_node (
	id int not null,
	compute_host_id int not null,
	node_name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_cmpt_node2cmpt_host
	    foreign key (compute_host_id) references rsk_compute_host (id),
	    
	constraint rsk_chk_uq_compute_node unique (node_name)
);

create table rsk_opengamma_version (
	id int not null,
	version varchar(255) not null, 
	
	primary key (id),
	
	constraint rsk_chk_uq_opengamma_version unique (version)
);

-- DBTOOLDONOTCLEAR
create table rsk_computation_target_type (
	id int not null,	 	            
    name varchar(255) not null,
    
    primary key (id),
    
    constraint rsk_chk_cmpt_target_type check
        ((id = 0 and name = 'PORTFOLIO_NODE') or
         (id = 1 and name = 'POSITION') or 
         (id = 2 and name = 'SECURITY') or
         (id = 3 and name = 'PRIMITIVE'))
);

insert into rsk_computation_target_type (id, name) values (0, 'PORTFOLIO_NODE');
insert into rsk_computation_target_type (id, name) values (1, 'POSITION');
insert into rsk_computation_target_type (id, name) values (2, 'SECURITY');
insert into rsk_computation_target_type (id, name) values (3, 'PRIMITIVE');

create table rsk_computation_target (
	id int not null,
	type_id int not null,
	id_scheme varchar(255) not null,
	id_value varchar(255) not null,
	id_version varchar(255) null,
	name varchar(255) null,
	
	primary key (id),
	
	constraint rsk_fk_cmpt_target2tgt_type 
	    foreign key (type_id) references rsk_computation_target_type (id),
	    
	constraint rsk_chk_uq_computation_target unique (type_id, id_scheme, id_value, id_version)
);

create table rsk_function_unique_id (
	id int not null,
	unique_id varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_function_unique_id unique (unique_id)
);

-------------------------------------
-- LiveData inputs
-------------------------------------

create table rsk_live_data_field (
	id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_live_data_field unique (name)
);

create table rsk_live_data_snapshot (
	id int not null,
	observation_datetime_id int not null,
	
	primary key (id),
	
	constraint rsk_fk_lv_data_snap2ob_dttime
	    foreign key (observation_datetime_id) references rsk_observation_datetime (id),
	    
	constraint rsk_chk_uq_live_data_snapshot unique (observation_datetime_id)
);

create table rsk_live_data_snapshot_entry (
	id bigint not null,
	snapshot_id int not null,
	computation_target_id int not null,
	field_id int not null,
	value double precision,
	
	primary key (id),
	
	constraint rsk_fk_snpsht_entry2snpsht
		foreign key (snapshot_id) references rsk_live_data_snapshot (id),
	constraint rsk_fk_spsht_entry2cmp_target
	    foreign key (computation_target_id) references rsk_computation_target (id),
	    
	constraint rsk_chk_uq_snapshot_entry unique (snapshot_id, computation_target_id, field_id) 	
);

-------------------------------------
-- Risk run
-------------------------------------

create table rsk_run (
    id int not null,
    opengamma_version_id int not null,
    master_process_host_id int not null,    -- machine where 'master' batch process was started
    run_time_id int not null,
    live_data_snapshot_id int not null,
    create_instant timestamp without time zone not null,
    start_instant timestamp without time zone not null,       -- can be different from create_instant if is run is restarted
    end_instant	timestamp without time zone,
    num_restarts int not null,
    complete boolean not null,
    
    primary key (id),
    
    constraint rsk_fk_run2opengamma_version
        foreign key (opengamma_version_id) references rsk_opengamma_version (id),
    constraint rsk_fk_run2compute_host
        foreign key (master_process_host_id) references rsk_compute_host (id),
    constraint rsk_fk_run2obs_datetime
        foreign key (run_time_id) references rsk_observation_datetime (id),
    constraint rsk_fk_run2live_data_snapshot
        foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id),

    constraint rsk_chk_uq_run unique (run_time_id)
);

create table rsk_calculation_configuration (
	id int not null,
	run_id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_calc_conf2run
	    foreign key (run_id) references rsk_run (id),
	
	constraint rsk_chk_uq_calc_conf unique (run_id, name)
);

-- Properties should be filled once only. If already there, use existing value.
--
-- Example properties:
-- 	- PositionMasterTime = 20100615170000
--  - GlobalRandomSeed = 54321
create table rsk_run_property (		
	id int not null,
	run_id int not null,
	property_key varchar(255) not null,
	property_value varchar(2000) not null,		    -- varchar(255) not enough
	
	primary key (id),

	constraint rsk_fk_run_property2run 
	    foreign key (run_id) references rsk_run (id)
);

-- DBTOOLDONOTCLEAR
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

create table rsk_run_status (
    id bigint not null, 
    calculation_configuration_id int not null,
    computation_target_id int not null,
    status int not null,

    constraint rsk_fk_run_status2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_run_status2comp_tgt
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_run_status2code
        foreign key (status) references rsk_run_status_code (id),

    constraint rsk_chk_uq_run_status unique (calculation_configuration_id, computation_target_id)
);


-------------------------------------
-- Risk
-------------------------------------

create table rsk_value_name (
    id int not null,
    name varchar(255) not null,
    
    primary key (id),
    
    constraint rsk_chk_uq_value_name unique (name)
);

create table rsk_value_specification (
    id int not null,
    synthetic_form varchar(1024) not null,

    primary key (id),

    constraint rsk_chk_uq_value_specification unique (synthetic_form)
);

create table rsk_value_requirement (
    id int not null,
    synthetic_form varchar(1024) not null,

    primary key (id),

    constraint rsk_chk_uq_value_requirement unique (synthetic_form)
);

create table rsk_value (
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,
    value_requirement_id int not null,
    value_specification_id int not null,
    function_unique_id int not null,
    computation_target_id int not null,        
    run_id int not null,             	       -- shortcut
    value double precision not null,
    eval_instant timestamp without time zone not null,
    compute_node_id int not null,
    
    primary key (id),
    
    -- performance implications of these requirement?
    constraint rsk_fk_value2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_value2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_value2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_value2value_requirement
        foreign key (value_requirement_id) references rsk_value_requirement (id),
    constraint rsk_fk_value2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_value2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_value2comp_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_value2compute_node
        foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_value unique (calculation_configuration_id, value_name_id, value_requirement_id, computation_target_id)
);


create table rsk_compute_failure (			
    id bigint not null,
    function_id varchar(255) not null,
    exception_class varchar(255) not null,
    exception_msg varchar(255) not null,                  
    stack_trace varchar(2000) not null,         -- first 2000 chars. not including msg
    
    primary key (id),
    
    constraint rsk_chk_uq_compute_failure unique (function_id, exception_class, exception_msg, stack_trace)
);

-- how to aggregate risk failures?
create table rsk_failure (			
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,
    value_requirement_id int not null,
    value_specification_id int not null,
    function_unique_id int not null,
    computation_target_id int not null,
    run_id int not null,             	       -- shortcut
    eval_instant timestamp without time zone not null,
    compute_node_id int not null,
    
    primary key (id),
    
    constraint rsk_fk_failure2calc_conf 
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_failure2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_failure2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_failure2value_requirement
        foreign key (value_requirement_id) references rsk_value_requirement (id),
    constraint rsk_fk_failure2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_failure2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_failure2com_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_failure2node
       foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_failure unique (calculation_configuration_id, value_name_id, value_requirement_id, computation_target_id)
);    

create table rsk_failure_reason (
   id bigint not null,
   rsk_failure_id bigint not null,
   compute_failure_id bigint not null,
   
   primary key (id),
   
   constraint rsk_fk_fail_reason2failure
       foreign key (rsk_failure_id) references rsk_failure (id)
       on delete cascade,
   constraint rsk_fk_fail_reason2cmpt_fail
       foreign key (compute_failure_id) references rsk_compute_failure (id),

   constraint rsk_chk_uq_failure_reason unique (rsk_failure_id, compute_failure_id)
);


-------------------------------------
-- Views
-------------------------------------

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
rsk_value_requirement.synthetic_form as requirement_synthetic_form,
rsk_value_specification.synthetic_form as specification_synthetic_form,
rsk_function_unique_id.unique_id as function_unique_id,
rsk_value.value,
rsk_value.eval_instant
from 
rsk_value, 
rsk_calculation_configuration,
rsk_value_name,
rsk_value_requirement,
rsk_value_specification,
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
rsk_value.value_requirement_id = rsk_value_requirement.id and
rsk_value.value_specification_id = rsk_value_specification.id and
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
rsk_value_requirement.synthetic_form as requirement_synthetic_form,
rsk_value_specification.synthetic_form as specification_synthetic_form,
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
rsk_value_requirement,
rsk_value_specification,
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
rsk_failure.value_requirement_id = rsk_value_requirement.id and
rsk_failure.value_specification_id = rsk_value_specification.id and
rsk_failure.function_unique_id = rsk_function_unique_id.id and
rsk_failure.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_failure.run_id = rsk_run.id and
rsk_failure.compute_node_id = rsk_compute_node.id and
rsk_run.run_time_id = rsk_observation_datetime.id and
rsk_observation_datetime.observation_time_id = rsk_observation_time.id and
rsk_failure_reason.rsk_failure_id = rsk_failure.id and
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;