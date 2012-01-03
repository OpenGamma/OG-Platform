-------------------------------------
-- Static data
-------------------------------------
CREATE SEQUENCE rsk_batch_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;


create table rsk_compute_host (
	id bigint not null,
	host_name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_compute_host unique (host_name)
);

create table rsk_compute_node (
	id bigint not null,
	compute_host_id bigint not null,
	node_name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_cmpt_node2cmpt_host
	    foreign key (compute_host_id) references rsk_compute_host (id),
	    
	constraint rsk_chk_uq_compute_node unique (node_name)
);

-- DBTOOLDONOTCLEAR
create table rsk_computation_target_type (
	id bigint not null,	 	            
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
	id bigint not null,
	type_id bigint not null,
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
	id bigint not null,
	unique_id varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_function_unique_id unique (unique_id)
);

-------------------------------------
-- LiveData inputs
-------------------------------------

create table rsk_live_data_field (
	id bigint not null,
	name varchar(255) not null,

	primary key (id),

	constraint rsk_chk_uq_live_data_field unique (name)
);

create table rsk_live_data_snapshot (
	id bigint not null,
	market_data_snapshot_uid varchar(255) not null,

	primary key (id),

	constraint rsk_chk_uq_live_data_snapshot unique (market_data_snapshot_uid)
);

create table rsk_live_data_snapshot_entry (
	id bigint not null,
	snapshot_id bigint not null,
	computation_target_id bigint not null,
	field_id bigint not null,
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

create table rsk_view_definition (
  id bigint not null,
  uid varchar(255) not null,

  primary key (id),

  constraint rsk_chk_uq_view_definition unique (uid)
);

create table rsk_version_correction (
  id bigint not null,
  as_of timestamp without time zone,
  corrected_to timestamp without time zone,

  primary key (id),

  constraint rsk_chk_uq_version_correction unique (as_of, corrected_to)
);

create table rsk_run (
    id bigint not null,
    version_correction_id bigint not null,
    view_definition_id bigint not null,
    live_data_snapshot_id bigint not null,
    create_instant timestamp without time zone not null,
    start_instant timestamp without time zone not null,       -- can be different from create_instant if is run is restarted
    end_instant	timestamp without time zone,
    valuation_time timestamp without time zone not null,
    num_restarts int not null,
    complete boolean not null,

    primary key (id),

    constraint rsk_fk_run2view_definition
        foreign key (view_definition_id) references rsk_view_definition (id),
    constraint rsk_fk_run2version_correction
        foreign key (version_correction_id) references rsk_version_correction (id),
    constraint rsk_fk_run2live_data_snapshot
        foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id),

    constraint rsk_chk_uq_run unique (version_correction_id, view_definition_id, live_data_snapshot_id)
);

create table rsk_calculation_configuration (
	id bigint not null,
	run_id bigint not null,
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
	id bigint not null,
	run_id bigint not null,
	property_key varchar(255) not null,
	property_value varchar(2000) not null,		    -- varchar(255) not enough
	
	primary key (id),

	constraint rsk_fk_run_property2run 
	    foreign key (run_id) references rsk_run (id)
);

-- DBTOOLDONOTCLEAR
create table rsk_run_status_code (
    id bigint not null,	 	            
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
    calculation_configuration_id bigint not null,
    computation_target_id bigint not null,
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
    id bigint not null,
    name varchar(255) not null,
    
    primary key (id),
    
    constraint rsk_chk_uq_value_name unique (name)
);

create table rsk_value_specification (
    id bigint not null,
    synthetic_form varchar(1024) not null,

    primary key (id),

    constraint rsk_chk_uq_value_specification unique (synthetic_form)
);

create table rsk_value_requirement (
    id bigint not null,
    synthetic_form varchar(1024) not null,
    specification_id bigint not null,

    primary key (id),

    constraint rsk_chk_uq_value_requirement unique (specification_id, synthetic_form)
);

create table rsk_value (
    id bigint not null,
    calculation_configuration_id bigint not null,
    value_name_id bigint not null,
    value_specification_id bigint not null,
    function_unique_id bigint not null,
    computation_target_id bigint not null,        
    run_id bigint not null,             	       -- shortcut
    value double precision not null,
    value_label varchar(255),
    eval_instant timestamp without time zone not null,
    compute_node_id bigint not null,
    
    primary key (id),
    
    -- performance implications of these requirement?
    constraint rsk_fk_value2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_value2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_value2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_value2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_value2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_value2comp_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_value2compute_node
        foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_value unique (calculation_configuration_id, value_label, value_name_id, value_specification_id, computation_target_id)
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
    calculation_configuration_id bigint not null,
    value_name_id bigint not null,
    value_specification_id bigint not null,
    function_unique_id bigint not null,
    computation_target_id bigint not null,
    run_id bigint not null,             	       -- shortcut
    eval_instant timestamp without time zone not null,
    compute_node_id bigint not null,
    
    primary key (id),
    
    constraint rsk_fk_failure2calc_conf 
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_failure2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_failure2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_failure2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_failure2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_failure2com_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_failure2node
       foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_failure unique (calculation_configuration_id, value_name_id, value_specification_id, computation_target_id)
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
rsk_view_definition.uid as view_definition_uid,
rsk_live_data_snapshot.market_data_snapshot_uid as market_data_snapshot_uid,
rsk_version_correction.as_of as as_of,
rsk_version_correction.corrected_to as corrected_to,
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
rsk_view_definition,
rsk_version_correction,
rsk_live_data_snapshot,
rsk_function_unique_id
where
rsk_run.view_definition_id = rsk_view_definition.id and
rsk_run.version_correction_id = rsk_version_correction.id and
rsk_run.live_data_snapshot_id = rsk_live_data_snapshot.id and
rsk_value.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_value.value_name_id = rsk_value_name.id and
rsk_value.value_specification_id = rsk_value_specification.id and
rsk_value.function_unique_id = rsk_function_unique_id.id and
rsk_value.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_value.run_id = rsk_run.id;

create view vw_rsk_failure as
select
rsk_computation_target_type.name as comp_target_type,
rsk_computation_target.id_scheme as comp_target_id_scheme,
rsk_computation_target.id_value as comp_target_id_value,
rsk_computation_target.id_version as comp_target_id_version,
rsk_computation_target.name as comp_target_name,
rsk_run.id as rsk_run_id,
rsk_view_definition.uid as view_definition_uid,
rsk_live_data_snapshot.market_data_snapshot_uid as market_data_snapshot_uid,
rsk_version_correction.as_of as as_of,
rsk_version_correction.corrected_to as corrected_to,
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
rsk_view_definition,
rsk_version_correction,
rsk_live_data_snapshot,
rsk_function_unique_id,
rsk_failure_reason,
rsk_compute_failure
where
rsk_run.view_definition_id = rsk_view_definition.id and
rsk_run.version_correction_id = rsk_version_correction.id and
rsk_run.live_data_snapshot_id = rsk_live_data_snapshot.id and
rsk_failure.calculation_configuration_id = rsk_calculation_configuration.id and
rsk_failure.value_name_id = rsk_value_name.id and
rsk_failure.value_specification_id = rsk_value_specification.id and
rsk_failure.function_unique_id = rsk_function_unique_id.id and
rsk_failure.computation_target_id = rsk_computation_target.id and
rsk_computation_target.type_id = rsk_computation_target_type.id and
rsk_failure.run_id = rsk_run.id and
rsk_failure_reason.rsk_failure_id = rsk_failure.id and
rsk_failure_reason.compute_failure_id = rsk_compute_failure.id;