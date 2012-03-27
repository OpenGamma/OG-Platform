START TRANSACTION;
  -- update the version
  UPDATE sec_schema_version SET version_value='46' WHERE version_key='schema_patch';
  
  -- update fx digitals
  ALTER TABLE sec_fxdigitaloption ADD COLUMN payment_currency_id;
  ALTER TABLE sec_fxdigitaloption ADD CONSTRAINT sec_fk_fxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id);
  -- copy put ccy to payment ccy for existing securities
  UPDATE sec_fxdigitaloption SET payment_currency_id=put_currency_id WHERE payment_currency_id IS NULL;
  -- make it a not-nullable column
  ALTER TABLE sec_fxdigitaloption ALTER COLUMN payment_currency_id SET NOT NULL;
  
  -- update ndf fx digitals
  ALTER TABLE sec_ndffxdigitaloption ADD COLUMN payment_currency_id;
  ALTER TABLE sec_ndffxdigitaloption ADD CONSTRAINT sec_fk_ndffxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id);
  -- copy put ccy to payment ccy for existing securities
  UPDATE sec_ndffxdigitaloption SET payment_currency_id=put_currency_id WHERE payment_currency_id IS NULL;
  -- make it a not-nullable column
  ALTER TABLE sec_ndffxdigitaloption ALTER COLUMN payment_currency_id SET NOT NULL;
  
COMMIT;