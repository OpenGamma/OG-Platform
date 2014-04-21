START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='67' WHERE version_key='schema_patch';

  -- add exchange initial notional column
  ALTER TABLE sec_swap ADD exchange_initial_notional boolean NULL;
  UPDATE sec_swap SET exchange_initial_notional = false;
  ALTER TABLE sec_swap ALTER COLUMN exchange_initial_notional SET NOT NULL;

  -- add exchange final notional column
  ALTER TABLE sec_swap ADD exchange_final_notional boolean NULL;
  UPDATE sec_swap SET exchange_final_notional = false;
  ALTER TABLE sec_swap ALTER COLUMN exchange_final_notional SET NOT NULL;

  -- add maturity tenor id column
  ALTER TABLE sec_swap ADD maturity_tenor_id bigint NULL;

  -- add maturity tenor constraint
  ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_sec_swapmaturitytenor2tenor FOREIGN KEY (maturity_tenor_id) REFERENCES sec_tenor (id);

  -- add pay conventional indexation lag column
  ALTER TABLE sec_swap ADD pay_conventional_indexation_lag integer NULL;

  -- add pay actual indexation lag column
  ALTER TABLE sec_swap ADD pay_actual_indexation_lag integer NULL;

  -- add pay interpolation method
  ALTER TABLE sec_swap ADD pay_index_interpolation_method varchar(32) NULL;

  -- add receive conventional indexation lag column
  ALTER TABLE sec_swap ADD receive_conventional_indexation_lag integer NULL;

  -- add receive actual indexation lag column
  ALTER TABLE sec_swap ADD receive_actual_indexation_lag integer NULL;

  -- add receive interpolation method
  ALTER TABLE sec_swap ADD receive_index_interpolation_method varchar(32) NULL;

COMMIT;
