ALTER TABLE sec_swaption ADD COLUMN is_payer BOOLEAN NOT NULL;
ALTER TABLE sec_swaption ADD COLUMN currency_id BIGINT NOT NULL;
ALTER TABLE sec_swaption ADD CONSTRAINT sec_fk_swaption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id);
