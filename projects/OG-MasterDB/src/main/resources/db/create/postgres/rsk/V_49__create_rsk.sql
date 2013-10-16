-------------------------------------
-- Versions
-------------------------------------

CREATE TABLE rsk_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO rsk_schema_version (version_key, version_value) VALUES ('schema_patch', '49');

CREATE SEQUENCE rsk_hibernate_sequence
    START WITH 1 INCREMENT BY 1;

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


create table rsk_computation_target (
	id bigint not null,
	type varchar(255) not null,	
    
    id_scheme varchar(255),
    id_value varchar(255),
    id_version varchar(255),
	primary key (id),
		    
	constraint rsk_chk_uq_computation_target unique (type, id_scheme, id_value, id_version)
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

create table rsk_live_data_snapshot (
	id bigint not null,
    base_uid_scheme  varchar(255) not null,
    base_uid_value   varchar(255) not null,
    base_uid_version varchar(255),
	primary key (id),

	constraint rsk_chk_uq_base_uid unique (base_uid_scheme, base_uid_value, base_uid_version)
);

create table rsk_live_data_snapshot_entry (
	id bigint not null,
	snapshot_id bigint not null,
	computation_target_id bigint not null,
    name  varchar(255) not null,
	value double precision,

	primary key (id),

	constraint rsk_fk_snpsht_entry2snpsht
		foreign key (snapshot_id) references rsk_live_data_snapshot (id),
	constraint rsk_fk_spsht_entry2cmp_target
	    foreign key (computation_target_id) references rsk_computation_target (id)
);

create table rsk_live_data_snapshot_entry_insertion (
	id bigint not null,
	snapshot_id bigint not null,
	computation_target_id bigint not null,
    name  varchar(255) not null,
	value double precision,

	primary key (id)
);


-------------------------------------
-- Risk run
-------------------------------------


create table rsk_run (
    id bigint not null,
    
    version_correction varchar(255) not null,
    viewdef_scheme      VARCHAR(255) NOT NULL,
    viewdef_value       VARCHAR(255) NOT NULL,
    viewdef_version     VARCHAR(255),
       
    live_data_snapshot_id bigint not null,
    
    create_instant timestamp without time zone not null,
    start_instant timestamp without time zone not null,       -- can be different from create_instant if is run is restarted
    end_instant	timestamp without time zone,
    valuation_time timestamp without time zone not null,
    num_restarts int not null,
    complete boolean not null,

    primary key (id),
    
    constraint rsk_fk_run2live_data_snapshot
            foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id),    

    constraint rsk_chk_uq_run unique (version_correction, viewdef_scheme, viewdef_value, viewdef_version, live_data_snapshot_id)
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


create table rsk_run_status (
    id bigint not null, 
    run_id bigint DEFAULT 0 not null,
    calculation_configuration_id bigint not null,
    computation_target_id bigint not null,
    status varchar(255) not null,

    constraint rsk_fk_run_status2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_run_status2comp_tgt
        foreign key (computation_target_id) references rsk_computation_target (id),    

    constraint rsk_chk_uq_run_status unique (run_id, calculation_configuration_id, computation_target_id)
);


-------------------------------------
-- Risk
-------------------------------------

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
    value_specification_id bigint not null,
    function_unique_id bigint not null,
    computation_target_id bigint not null,        
    run_id bigint not null,             	       -- shortcut
    value double precision not null,
    name varchar(255),
    eval_instant timestamp without time zone not null,
    compute_node_id bigint not null,
    
    primary key (id),
    
    -- performance implications of these requirement?
    constraint rsk_fk_value2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint rsk_fk_value2run 
        foreign key (run_id) references rsk_run (id), 
    constraint rsk_fk_value2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_value2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_value2comp_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_value2compute_node
        foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_value unique (run_id, calculation_configuration_id, name, value_specification_id, computation_target_id)
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
    name varchar(255),
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
    constraint rsk_fk_failure2value_specification
        foreign key (value_specification_id) references rsk_value_specification (id),
    constraint rsk_fk_failure2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id),
    constraint rsk_fk_failure2com_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint rsk_fk_failure2node
       foreign key (compute_node_id) references rsk_compute_node (id),
        
    constraint rsk_chk_uq_failure unique (calculation_configuration_id, name, value_specification_id, computation_target_id)
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
