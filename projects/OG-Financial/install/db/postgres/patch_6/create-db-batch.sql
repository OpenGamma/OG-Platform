-------------------------------------
-- Static data
-------------------------------------

create table rsk_observation_time (
    id int not null,
    label varchar(255) not null,                -- LDN_CLOSE
    
    primary key (id),
    
    unique (label)
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
	
	unique (date_part, observation_time_id)
);

create table rsk_compute_host (
	id int not null,
	host_name varchar(255) not null,
	
	primary key (id),
	
	unique (host_name)
);

create table rsk_compute_node (
	id int not null,
	config_oid varchar(255) not null,
	config_version int not null,
	compute_host_id int not null,
	node_name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_cmpt_node2cmpt_host
	    foreign key (compute_host_id) references rsk_compute_host (id),
	    
	unique (config_oid, config_version)
);

create table rsk_opengamma_version (
	id int not null,
	version varchar(255) not null, 
	hash varchar(255) not null,
	
	primary key (id),
	
	unique (version, hash)
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
	
	primary key (id),
	
	constraint rsk_fk_cmpt_target2tgt_type 
	    foreign key (type_id) references rsk_computation_target_type (id),
	    
	unique (type_id, id_scheme, id_value)
);

-------------------------------------
-- LiveData inputs
-------------------------------------

create table rsk_live_data_field (
	id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	unique(name)
);

create table rsk_live_data_snapshot (
	id int not null,
	observation_datetime_id int not null,
	complete boolean not null,
	
	primary key (id),
	
	constraint rsk_fk_lv_data_snap2ob_dttime
	    foreign key (observation_datetime_id) references rsk_observation_datetime (id),
	    
	unique (observation_datetime_id)
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
	    
	unique (snapshot_id, computation_target_id, field_id) 	
);

-------------------------------------
-- Risk run
-------------------------------------

create table rsk_run (
	id int not null,
	opengamma_version_id int not null,
	master_process_host_id int not null,    -- machine where 'master' batch process was started
    run_reason varchar(255) not null,       -- 15 June main overnight batch run
    run_time_id int not null,
    valuation_time timestamp not null,	 	-- 15 June 2010 17:00:00 - 'T'
    view_oid varchar(255) not null,
    view_version int not null,
    live_data_snapshot_id int not null,
    create_instant timestamp not null,
    start_instant timestamp not null,       -- can be different from create_instant if is run is restarted
    end_instant	timestamp,
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
        foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id)
);

create table rsk_calculation_configuration (
	id int not null,
	run_id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_calc_conf2run
	    foreign key (run_id) references rsk_run (id),
	
	unique (run_id, name)
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

create table rsk_run_status (
    id bigint not null, 
    calculation_configuration_id int not null,
    computation_target_id int not null,
    status int not null,

    constraint rsk_fk_run_status2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_run_status2comp_tgt
        foreign key (computation_target_id) references rsk_computation_target (id),

    unique (calculation_configuration_id, computation_target_id)
);

-------------------------------------
-- Risk
-------------------------------------

create table rsk_value_name (
    id int not null,
    name varchar(255) not null,
    
    primary key (id),
    
    unique (name)
);

create table rsk_value (
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,                 
    computation_target_id int not null,        
    run_id int not null,             	       -- shortcut
    value double precision not null,
    eval_instant timestamp not null,
    compute_node_id int not null,
    
    primary key (id),
    
    -- performance implications of these constraints?
    constraint rsk_fk_value2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_value2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_value2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_value2comp_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_value2compute_node
        foreign key (compute_node_id) references rsk_compute_node (id),
        
    unique (calculation_configuration_id, value_name_id, computation_target_id)
);


create table rsk_compute_failure (			
    id bigint not null,
    function_id varchar(255) not null,
    exception_class varchar(255) not null,
    exception_msg varchar(255) not null,                  
    stack_trace varchar(2000) not null,         -- first 2000 chars. not including msg
    
    primary key (id),
    
    unique (function_id, exception_class, exception_msg, stack_trace)
);

-- how to aggregate risk failures?
create table rsk_failure (			
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,                 
    computation_target_id int not null,
    run_id int not null,             	       -- shortcut
    eval_instant timestamp not null,
    compute_node_id int not null,
    
    primary key (id),
    
    constraint rsk_fk_failure2calc_conf 
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_failure2run 
        foreign key (run_id) references rsk_run (id),
    constraint rsk_fk_failure2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint rsk_fk_failure2com_target
        foreign key (computation_target_id) references rsk_computation_target (id),
   constraint rsk_fk_failure2node
       foreign key (compute_node_id) references rsk_compute_node (id),
        
    unique (calculation_configuration_id, value_name_id, computation_target_id)
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

   unique (rsk_failure_id, compute_failure_id)
);
