ALTER TABLE sec_swaption ADD COLUMN option_exercise_type VARCHAR(32);
ALTER TABLE sec_swaption ADD COLUMN settlement_date TIMESTAMP;
ALTER TABLE sec_swaption ADD COLUMN settlement_zone VARCHAR(50);
ALTER TABLE sec_swaption ADD COLUMN notional DOUBLE PRECISION;   
