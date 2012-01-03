-------------------------------------
-- Drop dependent objects
-------------------------------------


DROP VIEW vw_rsk;

DROP VIEW vw_rsk_failure;



-------------------------------------
CREATE SEQUENCE rsk_batch_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;


ALTER TABLE rsk_live_data_snapshot DROP CONSTRAINT rsk_fk_lv_data_snap2ob_dttime;
ALTER TABLE rsk_live_data_snapshot DROP CONSTRAINT rsk_chk_uq_live_data_snapshot;
ALTER TABLE rsk_live_data_snapshot DROP COLUMN observation_datetime_id;
 
ALTER TABLE rsk_live_data_snapshot ADD COLUMN market_data_snapshot_uid VARCHAR(255) NOT NULL;
ALTER TABLE rsk_live_data_snapshot ADD CONSTRAINT rsk_chk_uq_live_data_snapshot UNIQUE (market_data_snapshot_uid);

ALTER TABLE rsk_run DROP CONSTRAINT rsk_fk_run2obs_datetime;
DROP TABLE rsk_observation_datetime;
DROP TABLE rsk_observation_time;

ALTER TABLE rsk_run DROP CONSTRAINT rsk_fk_run2opengamma_version;
DROP TABLE rsk_opengamma_version;

-------------------------------------
ALTER TABLE rsk_calculation_configuration ALTER COLUMN run_id TYPE bigint

-------------------------------------
-- Risk run
-------------------------------------

CREATE TABLE rsk_view_definition (
  id bigint not null,
  uid varchar(255) not null,

  primary key (id),

  constraint rsk_chk_uq_view_definition unique (uid)
);

CREATE TABLE rsk_version_correction (
  id bigint not null,
  as_of timestamp without time zone,
  corrected_to timestamp without time zone,

  primary key (id),

  constraint rsk_chk_uq_version_correction unique (as_of, corrected_to)
);


ALTER TABLE rsk_run DROP COLUMN opengamma_version_id;

ALTER TABLE rsk_run DROP CONSTRAINT rsk_fk_run2compute_host;
ALTER TABLE rsk_run DROP COLUMN master_process_host_id;


ALTER TABLE rsk_run DROP CONSTRAINT rsk_chk_uq_run;
ALTER TABLE rsk_run DROP COLUMN run_time_id;


ALTER TABLE rsk_run ADD COLUMN valuation_time timestamp without time zone NOT NULL;


ALTER TABLE rsk_run ADD COLUMN version_correction_id BIGINT NOT NULL;
ALTER TABLE rsk_run ADD CONSTRAINT rsk_fk_run2version_correction FOREIGN KEY (version_correction_id) REFERENCES rsk_version_correction (id);

ALTER TABLE rsk_run ADD COLUMN view_definition_id BIGINT NOT NULL;
ALTER TABLE rsk_run ADD CONSTRAINT rsk_fk_run2view_definition FOREIGN KEY (view_definition_id) REFERENCES rsk_view_definition (id);

ALTER TABLE rsk_run ADD CONSTRAINT rsk_chk_uq_run UNIQUE (version_correction_id, view_definition_id, live_data_snapshot_id);

-------------------------------------
-- Changing ID column types from int to bigint
-------------------------------------

ALTER TABLE rsk_compute_host ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_compute_node ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_compute_node ALTER COLUMN compute_host_id TYPE bigint

ALTER TABLE rsk_computation_target_type ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_computation_target ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_computation_target ALTER COLUMN type_id TYPE bigint

ALTER TABLE rsk_function_unique_id ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_live_data_field ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_live_data_snapshot ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN snapshot_id TYPE bigint
ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN computation_target_id TYPE bigint
ALTER TABLE rsk_live_data_snapshot_entry ALTER COLUMN field_id TYPE bigint

ALTER TABLE rsk_view_definition ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_version_correction ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_run ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_run ALTER COLUMN version_correction_id TYPE bigint
ALTER TABLE rsk_run ALTER COLUMN view_definition_id TYPE bigint
ALTER TABLE rsk_run ALTER COLUMN live_data_snapshot_id TYPE bigint

ALTER TABLE rsk_calculation_configuration ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_run_property ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_run_property ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_run_status_code ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_run_status ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_run_status ALTER COLUMN calculation_configuration_id TYPE bigint
ALTER TABLE rsk_run_status ALTER COLUMN computation_target_id TYPE bigint

ALTER TABLE rsk_value_name ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_value_specification ALTER COLUMN id TYPE bigint

ALTER TABLE rsk_value_requirement ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_value_requirement ADD COLUMN specification_id BIGINT NOT NULL;
ALTER TABLE rsk_value_requirement DROP CONSTRAINT rsk_chk_uq_value_requirement;
ALTER TABLE rsk_value_requirement ADD CONSTRAINT rsk_chk_uq_value_requirement unique (specification_id, synthetic_form);

ALTER TABLE rsk_value DROP CONSTRAINT rsk_chk_uq_value;
ALTER TABLE rsk_value DROP CONSTRAINT rsk_fk_value2value_requirement;
ALTER TABLE rsk_value DROP COLUMN value_requirement_id;
ALTER TABLE rsk_value ADD COLUMN value_label varchar(255);
ALTER TABLE rsk_value ADD CONSTRAINT rsk_chk_uq_value unique (calculation_configuration_id, value_label, value_name_id, value_specification_id, computation_target_id);

ALTER TABLE rsk_value ALTER COLUMN id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN calculation_configuration_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN value_name_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN value_requirement_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN value_specification_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN function_unique_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN computation_target_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN run_id TYPE bigint
ALTER TABLE rsk_value ALTER COLUMN compute_node_id TYPE bigint

ALTER TABLE rsk_compute_failure ALTER COLUMN id SET DATA TYPE bigint

ALTER TABLE rsk_failure ALTER COLUMN id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN calculation_configuration_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN value_name_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN value_requirement_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN value_specification_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN function_unique_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN computation_target_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN run_id SET DATA TYPE bigint
ALTER TABLE rsk_failure ALTER COLUMN compute_node_id SET DATA TYPE bigint

ALTER TABLE rsk_failure_reason ALTER COLUMN id SET DATA TYPE bigint
ALTER TABLE rsk_failure_reason ALTER COLUMN rsk_failure_id SET DATA TYPE bigint
ALTER TABLE rsk_failure_reason ALTER COLUMN compute_failure_id SET DATA TYPE bigint

ALTER TABLE rsk_failure DROP CONSTRAINT rsk_chk_uq_failure;
ALTER TABLE rsk_failure DROP CONSTRAINT rsk_fk_failure2value_requirement;
ALTER TABLE rsk_failure DROP COLUMN value_requirement_id;
ALTER TABLE rsk_failure ADD CONSTRAINT rsk_chk_uq_failure unique (calculation_configuration_id, value_name_id, value_specification_id, computation_target_id);
        
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