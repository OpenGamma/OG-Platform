BEGIN;
  -- fields for handling variance swaps using the existing swap security
  ALTER TABLE sec_swap ADD COLUMN pay_strike DOUBLE PRECISION;
  ALTER TABLE sec_swap ADD COLUMN pay_variance_swap_type VARCHAR(32);
  ALTER TABLE sec_swap ADD COLUMN pay_underlying_identifier VARCHAR(255);
  ALTER TABLE sec_swap ADD COLUMN pay_underlying_scheme VARCHAR(255);
  ALTER TABLE sec_swap ADD COLUMN pay_monitoring_frequency_id BIGINT;
  ALTER TABLE sec_swap ADD COLUMN pay_annualization_factor DOUBLE PRECISION;
  ALTER TABLE sec_swap ADD COLUMN receive_strike DOUBLE PRECISION;
  ALTER TABLE sec_swap ADD COLUMN receive_variance_swap_type VARCHAR(32);
  ALTER TABLE sec_swap ADD COLUMN receive_underlying_identifier VARCHAR(255);
  ALTER TABLE sec_swap ADD COLUMN receive_underlying_scheme VARCHAR(255);
  ALTER TABLE sec_swap ADD COLUMN receive_monitoring_frequency_id BIGINT;
  ALTER TABLE sec_swap ADD COLUMN receive_annualization_factor DOUBLE PRECISION;
  ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_paymonitorfreq2frequency FOREIGN KEY (pay_monitoring_frequency_id) REFERENCES sec_frequency (id);
  ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_recvmonitorfreq2frequency FOREIGN KEY (receive_monitoring_frequency_id) REFERENCES sec_frequency (id);

  UPDATE sec_schema_version SET version_value='44' WHERE version_key='schema_patch';
COMMIT;