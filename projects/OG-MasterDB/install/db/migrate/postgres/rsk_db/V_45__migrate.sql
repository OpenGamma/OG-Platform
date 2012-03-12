-- VERSION TABLES

-- version data tables for each module.  At the moment the version numbers are in sync across
-- the modules, but they are likely to diverge over time.
-- YOU MUST UPDATE THESE VERSION NUMBERS FOR NEW VERSIONS OF THE SCHEMA
-- a template set of update statements is commented out below (set to the NEXT verison number)
-- UPDATE rsk_schema_version SET version_value='45' WHERE version_key='schema_patch';

BEGIN;

  UPDATE rsk_schema_version SET version_value='45' WHERE version_key='schema_patch';
  
COMMIT;
--------------------------------------------------------------------------------------

BEGIN;
ALTER TABLE rsk_calculation_configuration DROP CONSTRAINT rsk_fk_calc_conf2run;   
ALTER TABLE rsk_computation_target        DROP CONSTRAINT rsk_chk_uq_computation_target;
ALTER TABLE rsk_computation_target        DROP CONSTRAINT rsk_fk_cmpt_target2tgt_type;
ALTER TABLE rsk_compute_node              DROP CONSTRAINT rsk_fk_cmpt_node2cmpt_host;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2com_target;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2run;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_chk_uq_failure;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2node;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2value_requirement;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2calc_conf;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2function_id;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2value_name;
ALTER TABLE rsk_failure                   DROP CONSTRAINT rsk_fk_failure2value_specification;
ALTER TABLE rsk_live_data_snapshot        DROP CONSTRAINT rsk_chk_uq_live_data_snapshot;
ALTER TABLE rsk_live_data_snapshot        DROP CONSTRAINT rsk_fk_lv_data_snap2ob_dttime;
ALTER TABLE rsk_live_data_snapshot_entry  DROP CONSTRAINT rsk_fk_spsht_entry2cmp_target;
ALTER TABLE rsk_live_data_snapshot_entry  DROP CONSTRAINT rsk_fk_snpsht_entry2snpsht;
ALTER TABLE rsk_live_data_snapshot_entry  DROP CONSTRAINT rsk_chk_uq_snapshot_entry;
ALTER TABLE rsk_run                       DROP CONSTRAINT rsk_chk_uq_run;
ALTER TABLE rsk_run                       DROP CONSTRAINT rsk_fk_run2compute_host;
ALTER TABLE rsk_run                       DROP CONSTRAINT rsk_fk_run2live_data_snapshot;    
ALTER TABLE rsk_run                       DROP CONSTRAINT rsk_fk_run2obs_datetime;
ALTER TABLE rsk_run                       DROP CONSTRAINT rsk_fk_run2opengamma_version;
ALTER TABLE rsk_run_property              DROP CONSTRAINT rsk_fk_run_property2run;
ALTER TABLE rsk_run_status                DROP CONSTRAINT rsk_fk_run_status2comp_tgt;
ALTER TABLE rsk_run_status                DROP CONSTRAINT rsk_fk_run_status2code;
ALTER TABLE rsk_run_status                DROP CONSTRAINT rsk_fk_run_status2calc_conf;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2comp_target;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2function_id;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2run;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2calc_conf;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_chk_uq_value;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2compute_node;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2value_requirement;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2value_name;
ALTER TABLE rsk_value                     DROP CONSTRAINT rsk_fk_value2value_specification;
ALTER TABLE rsk_value_requirement         DROP CONSTRAINT rsk_chk_uq_value_requirement;
--//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
DROP VIEW vw_rsk;
DROP VIEW vw_rsk_failure;

DROP TABLE rsk_computation_target_type;
DROP TABLE rsk_observation_datetime;
DROP TABLE rsk_observation_time;
DROP TABLE rsk_opengamma_version;
DROP TABLE rsk_run_status_code;
DROP TABLE rsk_live_data_field;
DROP TABLE rsk_value_name;

-------------------------------------;
CREATE SEQUENCE rsk_batch_seq START WITH 1000 INCREMENT BY 1 NO CYCLE;

ALTER TABLE rsk_live_data_snapshot DROP COLUMN observation_datetime_id;
ALTER TABLE rsk_live_data_snapshot ADD COLUMN base_uid_scheme VARCHAR(255) NOT NULL;
ALTER TABLE rsk_live_data_snapshot ADD COLUMN base_uid_value VARCHAR(255) NOT NULL;
ALTER TABLE rsk_live_data_snapshot ADD COLUMN base_uid_version VARCHAR(255);

ALTER TABLE rsk_run DROP COLUMN opengamma_version_id;
ALTER TABLE rsk_run DROP COLUMN master_process_host_id;
ALTER TABLE rsk_run DROP COLUMN run_time_id;
ALTER TABLE rsk_run ADD COLUMN valuation_time timestamp without time zone NOT NULL;
ALTER TABLE rsk_run ADD COLUMN version_correction VARCHAR(255) NOT NULL;
ALTER TABLE rsk_run ADD COLUMN viewdef_scheme VARCHAR(255) NOT NULL;
ALTER TABLE rsk_run ADD COLUMN viewdef_value VARCHAR(255) NOT NULL;
ALTER TABLE rsk_run ADD COLUMN viewdef_version VARCHAR(255);
ALTER TABLE rsk_run ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_run ALTER COLUMN live_data_snapshot_id TYPE bigint;


ALTER TABLE rsk_calculation_configuration ALTER COLUMN run_id TYPE bigint;
ALTER TABLE rsk_calculation_configuration ALTER COLUMN id TYPE bigint;

ALTER TABLE rsk_compute_host ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_compute_node ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_compute_node ALTER COLUMN compute_host_id TYPE bigint;

ALTER TABLE rsk_computation_target DROP COLUMN type_id;
ALTER TABLE rsk_computation_target DROP COLUMN name;
ALTER TABLE rsk_computation_target ADD COLUMN type VARCHAR(255) NOT NULL;
ALTER TABLE rsk_computation_target ALTER COLUMN id TYPE bigint;

ALTER TABLE rsk_function_unique_id ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_live_data_snapshot ALTER COLUMN id TYPE bigint;

ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN snapshot_id TYPE bigint;
ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN computation_target_id TYPE bigint;
ALTER TABLE rsk_live_data_snapshot_entry ADD COLUMN name VARCHAR(255) NOT NULL;	

ALTER TABLE rsk_live_data_snapshot_entry DROP COLUMN field_id;

create table rsk_live_data_snapshot_entry_insertion (
	id bigint not null,
	snapshot_id bigint not null,
	computation_target_id bigint not null,
    name  varchar(255) not null,
	value double precision,

	primary key (id)
);



ALTER TABLE rsk_run_property ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_run_property ALTER COLUMN run_id TYPE bigint;


ALTER TABLE rsk_run_status ALTER COLUMN status TYPE VARCHAR(255);

ALTER TABLE rsk_run_status ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_run_status ALTER COLUMN calculation_configuration_id TYPE bigint;
ALTER TABLE rsk_run_status ALTER COLUMN computation_target_id TYPE bigint;

ALTER TABLE rsk_value_specification ALTER COLUMN id TYPE bigint;

ALTER TABLE rsk_value_requirement ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_value_requirement ADD COLUMN specification_id BIGINT NOT NULL;

ALTER TABLE rsk_value DROP COLUMN value_requirement_id;
ALTER TABLE rsk_value ADD COLUMN name VARCHAR(255);

ALTER TABLE rsk_value DROP COLUMN value_name_id;
ALTER TABLE rsk_value ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN calculation_configuration_id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN value_specification_id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN function_unique_id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN computation_target_id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN run_id TYPE bigint;
ALTER TABLE rsk_value ALTER COLUMN compute_node_id TYPE bigint;

ALTER TABLE rsk_compute_failure ALTER COLUMN id TYPE bigint;

ALTER TABLE rsk_failure DROP COLUMN value_name_id;
ALTER TABLE rsk_failure DROP COLUMN value_requirement_id;
ALTER TABLE rsk_failure ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN calculation_configuration_id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN value_specification_id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN function_unique_id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN computation_target_id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN run_id TYPE bigint;
ALTER TABLE rsk_failure ALTER COLUMN compute_node_id TYPE bigint;
ALTER TABLE rsk_failure ADD COLUMN name VARCHAR(255);

ALTER TABLE rsk_failure_reason ALTER COLUMN id TYPE bigint;
ALTER TABLE rsk_failure_reason ALTER COLUMN rsk_failure_id TYPE bigint;
ALTER TABLE rsk_failure_reason ALTER COLUMN compute_failure_id TYPE bigint;



--//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
ALTER TABLE rsk_calculation_configuration ADD CONSTRAINT rsk_fk_calc_conf2run  foreign key (run_id) references rsk_run (id);
ALTER TABLE rsk_computation_target        ADD CONSTRAINT rsk_chk_uq_computation_target UNIQUE (type, id_scheme, id_value, id_version);
ALTER TABLE rsk_compute_node              ADD CONSTRAINT rsk_fk_cmpt_node2cmpt_host foreign key (compute_host_id) references rsk_compute_host (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2com_target foreign key (computation_target_id) references rsk_computation_target (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2run      foreign key (run_id) references rsk_run (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2node foreign key (compute_node_id) references rsk_compute_node (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_chk_uq_failure unique (calculation_configuration_id, name, value_specification_id, computation_target_id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2calc_conf foreign key (calculation_configuration_id) references rsk_calculation_configuration (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2function_id foreign key (function_unique_id) references rsk_function_unique_id (id);
ALTER TABLE rsk_failure                   ADD CONSTRAINT rsk_fk_failure2value_specification foreign key (value_specification_id) references rsk_value_specification (id);
ALTER TABLE rsk_live_data_snapshot        ADD CONSTRAINT rsk_chk_uq_base_uid unique (base_uid_scheme, base_uid_value, base_uid_version);
ALTER TABLE rsk_live_data_snapshot_entry  ADD CONSTRAINT rsk_fk_spsht_entry2cmp_target foreign key (computation_target_id) references rsk_computation_target (id);
ALTER TABLE rsk_live_data_snapshot_entry  ADD CONSTRAINT rsk_fk_snpsht_entry2snpsht foreign key (snapshot_id) references rsk_live_data_snapshot (id);
ALTER TABLE rsk_live_data_snapshot_entry  ADD CONSTRAINT rsk_chk_uq_snapshot_entry unique (snapshot_id, computation_target_id);
ALTER TABLE rsk_run                       ADD CONSTRAINT rsk_chk_uq_run UNIQUE (version_correction, viewdef_scheme, viewdef_value, viewdef_version, live_data_snapshot_id);
ALTER TABLE rsk_run                       ADD CONSTRAINT rsk_fk_run2live_data_snapshot        foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id); 
ALTER TABLE rsk_run_property              ADD CONSTRAINT rsk_fk_run_property2run foreign key (run_id) references rsk_run (id);
ALTER TABLE rsk_run_status                ADD CONSTRAINT rsk_fk_run_status2comp_tgt foreign key (computation_target_id) references rsk_computation_target (id);
ALTER TABLE rsk_run_status                ADD CONSTRAINT rsk_fk_run_status2calc_conf foreign key (calculation_configuration_id) references rsk_calculation_configuration (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2comp_target foreign key (computation_target_id) references rsk_computation_target (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2run        foreign key (run_id) references rsk_run (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2compute_node foreign key (compute_node_id) references rsk_compute_node (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_chk_uq_value unique (calculation_configuration_id, name, value_specification_id, computation_target_id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2calc_conf foreign key (calculation_configuration_id) references rsk_calculation_configuration (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2value_specification foreign key (value_specification_id) references rsk_value_specification (id);
ALTER TABLE rsk_value                     ADD CONSTRAINT rsk_fk_value2function_id foreign key (function_unique_id) references rsk_function_unique_id (id);
ALTER TABLE rsk_value_requirement         ADD CONSTRAINT rsk_chk_uq_value_requirement unique (specification_id, synthetic_form);
COMMIT;

