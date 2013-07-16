START TRANSACTION;
  -- update the version
  UPDATE sec_schema_version SET version_value='46' WHERE version_key='schema_patch';
  
  -- update fx digitals, make it a not-nullable 
  ALTER TABLE sec_fxdigitaloption ADD COLUMN payment_currency_id BIGINT NOT NULL;
  -- copy put ccy to payment ccy for existing securities
  UPDATE sec_fxdigitaloption SET payment_currency_id=put_currency_id WHERE payment_currency_id IS NULL;
  -- add FK constraint
  ALTER TABLE sec_fxdigitaloption ADD CONSTRAINT sec_fk_fxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id);
  
  -- update ndf fx digitals, make it a not-nullable column
  ALTER TABLE sec_ndffxdigitaloption ADD COLUMN payment_currency_id BIGINT NOT NULL;  
  -- copy put ccy to payment ccy for existing securities
  UPDATE sec_ndffxdigitaloption SET payment_currency_id=put_currency_id WHERE payment_currency_id IS NULL;  
  ALTER TABLE sec_ndffxdigitaloption ADD CONSTRAINT sec_fk_ndffxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id);
  
  ALTER TABLE sec_ndffxdigitaloption DROP CONSTRAINT sec_fk_fxndfdigitaloption2callcurrency;
  ALTER TABLE sec_ndffxdigitaloption DROP CONSTRAINT sec_fk_fxndfdigitaloption2putcurrency;
  ALTER TABLE sec_ndffxdigitaloption DROP CONSTRAINT sec_fk_fxndfdigitaloption2sec;
  ALTER TABLE sec_ndffxdigitaloption ADD CONSTRAINT sec_fk_ndffxdigitaloption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id);
  ALTER TABLE sec_ndffxdigitaloption ADD CONSTRAINT sec_fk_ndffxdigitaloption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id);
  ALTER TABLE sec_ndffxdigitaloption ADD CONSTRAINT sec_fk_ndffxdigitaloption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id);

  COMMIT;