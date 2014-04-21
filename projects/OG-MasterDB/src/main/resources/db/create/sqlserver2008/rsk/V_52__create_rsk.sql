-------------------------------------
-- Versions
-------------------------------------

CREATE TABLE rsk_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO rsk_schema_version (version_key, version_value) VALUES ('schema_patch', '52');

CREATE TABLE rsk_hibernate_sequence (
  next_val numeric(19,0) 
)

-- CREATE SEQUENCE rsk_batch_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE TABLE rsk_batch_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)

create table rsk_compute_host (
	id BIGINT not null,
	host_name VARCHAR(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_compute_host unique (host_name)
);

create table rsk_compute_node (
	id BIGINT not null,
	compute_host_id BIGINT not null,
	node_name VARCHAR(255) not null,
	
	primary key (id),
	
	constraint rsk_fk_cmpt_node2cmpt_host
	    foreign key (compute_host_id) references rsk_compute_host (id),
	    
	constraint rsk_chk_uq_compute_node unique (node_name)
);


create table rsk_computation_target (
	id BIGINT not null,
	type VARCHAR(255) not null,	
    
    id_scheme VARCHAR(255),
    id_value VARCHAR(255),
    id_version VARCHAR(255),
	primary key (id),
		    
	constraint rsk_chk_uq_computation_target unique (type, id_scheme, id_value, id_version)
);

create table rsk_target_property (
	id bigint not null,
	target_id bigint not null,
  property_key varchar(255),
  property_value varchar(255),
	primary key (id),

	constraint rsk_fk_trg_prop2target
	    foreign key (target_id) references rsk_computation_target (id)
);

create table rsk_function_unique_id (
	id BIGINT not null,
	unique_id VARCHAR(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_function_unique_id unique (unique_id)
);

-------------------------------------
-- LiveData inputs
-------------------------------------

create table rsk_live_data_snapshot (
	id BIGINT not null,
    base_uid_scheme  VARCHAR(255) not null,
    base_uid_value   VARCHAR(255) not null,
    base_uid_version VARCHAR(255),
	primary key (id),

	constraint rsk_chk_uq_base_uid unique (base_uid_scheme, base_uid_value, base_uid_version)
);

create table rsk_live_data_snapshot_entry (
	id BIGINT not null,
	snapshot_id BIGINT not null,
	computation_target_id BIGINT not null,
    name  VARCHAR(255) not null,
	value double precision,

	primary key (id),

	constraint rsk_fk_snpsht_entry2snpsht
		foreign key (snapshot_id) references rsk_live_data_snapshot (id),
	constraint rsk_fk_spsht_entry2cmp_target
	    foreign key (computation_target_id) references rsk_computation_target (id)
);

create table rsk_live_data_snapshot_entry_insertion (
	id BIGINT not null,
	snapshot_id BIGINT not null,
	computation_target_id BIGINT not null,
    name  VARCHAR(255) not null,
	value double precision,

	primary key (id)
);


-------------------------------------
-- Risk run
-------------------------------------


create table rsk_run (
    id BIGINT not null,
    
    version_correction VARCHAR(255) not null,
    viewdef_scheme      VARCHAR(255) NOT NULL,
    viewdef_value       VARCHAR(255) NOT NULL,
    viewdef_version     VARCHAR(255),
    cycle_name     VARCHAR(255),
       
    live_data_snapshot_id BIGINT not null,
    
    create_instant DATETIME2(6) not null,
    start_instant DATETIME2(6) not null,       -- can be different from create_instant if is run is restarted
    end_instant	DATETIME2(6),
    valuation_time DATETIME2(6) not null,
    num_restarts int not null,
    complete bit not null,

    primary key (id),
    
    constraint rsk_fk_run2live_data_snapshot
            foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id),    

    constraint rsk_chk_uq_run unique (id, version_correction, viewdef_scheme, viewdef_value, viewdef_version, live_data_snapshot_id)
);

create table rsk_calculation_configuration (
	id BIGINT not null,
	run_id BIGINT not null,
	name VARCHAR(255) not null,
	
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
	id BIGINT not null,
	run_id BIGINT not null,
	property_key VARCHAR(255) not null,
	property_value VARCHAR(2000) not null,		    -- varchar(255) not enough
	
	primary key (id),

	constraint rsk_fk_run_property2run 
	    foreign key (run_id) references rsk_run (id)
);


create table rsk_run_status (
    id BIGINT not null, 
    run_id BIGINT not null,
    calculation_configuration_id BIGINT not null,
    computation_target_id BIGINT not null,
    status VARCHAR(255) not null,

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
    id BIGINT not null,
    synthetic_form VARCHAR(1024) not null,

    primary key (id),

    constraint rsk_chk_uq_value_specification unique (synthetic_form)
);

create table rsk_value_requirement (
    id BIGINT not null,
    synthetic_form VARCHAR(1024) not null,
    specification_id BIGINT not null,

    primary key (id),

    constraint rsk_chk_uq_value_requirement unique (specification_id, synthetic_form)
);

create table rsk_value (
    id BIGINT not null,
    calculation_configuration_id BIGINT not null,
    value_specification_id BIGINT not null,
    function_unique_id BIGINT not null,
    computation_target_id BIGINT not null,        
    run_id BIGINT not null,             	       -- shortcut
    value double precision not null,
    name VARCHAR(255),
    eval_instant DATETIME2(6) not null,
    compute_node_id BIGINT not null,
    
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
    id BIGINT not null,
    function_id VARCHAR(255) not null,
    exception_class VARCHAR(255) not null,
    exception_msg VARCHAR(255) not null,                  
    stack_trace VARCHAR(2000) not null,         -- first 2000 chars. not including msg
    
    primary key (id),
    
    constraint rsk_chk_uq_compute_failure unique (function_id, exception_class, exception_msg, stack_trace)
);

-- how to aggregate risk failures?
create table rsk_failure (			
    id BIGINT not null,
    calculation_configuration_id BIGINT not null,
    name VARCHAR(255),
    value_specification_id BIGINT not null,
    function_unique_id BIGINT not null,
    computation_target_id BIGINT not null,
    run_id BIGINT not null,             	       -- shortcut
    eval_instant DATETIME2(6) not null,
    compute_node_id BIGINT not null,
    
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
        
    constraint rsk_chk_uq_failure unique (run_id, calculation_configuration_id, name, value_specification_id, computation_target_id)
);    

create table rsk_failure_reason (
   id BIGINT not null,
   rsk_failure_id BIGINT not null,
   compute_failure_id BIGINT not null,
   
   primary key (id),
   
   constraint rsk_fk_fail_reason2failure
       foreign key (rsk_failure_id) references rsk_failure (id)
       on delete cascade,
   constraint rsk_fk_fail_reason2cmpt_fail
       foreign key (compute_failure_id) references rsk_compute_failure (id),

   constraint rsk_chk_uq_failure_reason unique (rsk_failure_id, compute_failure_id)
);
