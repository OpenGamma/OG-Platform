-- VERSION TABLES

-- version data tables for each module.  At the moment the version numbers are in sync across
-- the modules, but they are likely to diverge over time.
-- YOU MUST UPDATE THESE VERSION NUMBERS FOR NEW VERSIONS OF THE SCHEMA
-- a template set of update statements is commented out below (set to the NEXT verison number)
-- UPDATE rsk_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE cfg_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE eng_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE hts_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE snp_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE prt_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE pos_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE hol_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE exg_schema_version SET version_value="44" WHERE version_key="schema_patch";
-- UPDATE sec_schema_version SET version_value="44" WHERE version_key="schema_patch";

BEGIN;
  CREATE TABLE rsk_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO rsk_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE cfg_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO cfg_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE eng_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO eng_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE hts_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO hts_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE snp_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO snp_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE prt_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO prt_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE pos_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO pos_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE hol_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO hol_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE exg_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO exg_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
  CREATE TABLE sec_schema_version (
      version_key VARCHAR(32) NOT NULL,
      version_value VARCHAR(255) NOT NULL
  );
  INSERT INTO sec_schema_version (version_key, version_value) VALUES ('schema_patch', '43');
  
COMMIT;
--------------------------------------------------------------------------------------
-- CASH
-- Changes to cash securities to add start date/time and daycount fields.
BEGIN;
  --DELETE FROM sec_cash;
  ALTER TABLE sec_cash ADD COLUMN start_date TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL;
  ALTER TABLE sec_cash ADD COLUMN start_zone VARCHAR(50) DEFAULT 'UTC' NOT NULL;
  -- if the following line fails, either change the number to the result of 'SELECT id FROM sec_daycount LIMIT 1' or 
  -- uncomment the DELETE above and remove the DEFAULT 1 clause all together (which will delete existing cash securities).  
  ALTER TABLE sec_cash ADD COLUMN daycount_id BIGINT DEFAULT 1 NOT NULL;
  ALTER TABLE sec_cash ADD CONSTRAINT sec_fk_cash2daycount FOREIGN KEY (daycount_id) REFERENCES sec_daycount (id);
  ALTER TABLE sec_cash ALTER COLUMN start_date DROP DEFAULT;
  ALTER TABLE sec_cash ALTER COLUMN start_zone DROP DEFAULT;
COMMIT;
--------------------------------------------------------------------------------------
-- FX DIGITALS
CREATE TABLE sec_fxdigitaloption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id bigint NOT NULL,
    call_currency_id bigint NOT NULL,
    settlement_date timestamp without time zone NOT NULL,
    settlement_zone varchar(50) NOT NULL,
    is_long boolean NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxdigitaloption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxdigitaloption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxdigitaloption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);

CREATE INDEX ix_sec_fxdigitaloption_security_id ON sec_fxdigitaloption(security_id);

CREATE TABLE sec_ndffxdigitaloption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id bigint NOT NULL,
    call_currency_id bigint NOT NULL,
    settlement_date timestamp without time zone NOT NULL,
    settlement_zone varchar(50) NOT NULL,
    is_long boolean NOT NULL,
    is_delivery_in_call_currency boolean NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxndfdigitaloption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxndfdigitaloption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxndfdigitaloption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);

CREATE INDEX ix_sec_ndffxdigitaloption_security_id ON sec_ndffxdigitaloption(security_id);

-- Missing Index
CREATE INDEX ix_sec_fxoption_security_id ON sec_fxoption(security_id);
--------------------------------------------------------------------------------------
-- FIXING FX FORWARDS and NON-DELIVERABLE FX FORWARDS
-- Modify structure of sec_fxforward and sec_nondeliverablefxforward to remove underlying, copying any existing data from the underlying
-- sec_fx table.

BEGIN;
  -- Pay Currency
  ALTER TABLE sec_fxforward ADD COLUMN pay_currency_id BIGINT;
  
  UPDATE sec_fxforward SET pay_currency_id=(SELECT pay_currency_id FROM sec_fx
                                            LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                            LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                            WHERE keys.key_scheme=underlying_scheme AND
                                                  keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_fxforward ALTER COLUMN pay_currency_id SET NOT NULL;
  
  -- Receive Currency
  ALTER TABLE sec_fxforward ADD COLUMN receive_currency_id BIGINT;
  
  UPDATE sec_fxforward SET receive_currency_id=(SELECT receive_currency_id FROM sec_fx
                                                LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                                LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                                WHERE keys.key_scheme=underlying_scheme AND
                                                      keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_fxforward ALTER COLUMN receive_currency_id SET NOT NULL;
  
  -- Pay Amount
  ALTER TABLE sec_fxforward ADD COLUMN pay_amount DOUBLE PRECISION;
  
  UPDATE sec_fxforward SET pay_amount=(SELECT pay_amount FROM sec_fx
                                       LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                       LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                       WHERE keys.key_scheme=underlying_scheme AND
                                             keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_fxforward ALTER COLUMN pay_amount SET NOT NULL;
  -- Receive Amount
  ALTER TABLE sec_fxforward ADD COLUMN receive_amount DOUBLE PRECISION;
  
  UPDATE sec_fxforward SET receive_amount=(SELECT receive_amount FROM sec_fx
                                           LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                           LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                           WHERE keys.key_scheme=underlying_scheme AND
                                                 keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_fxforward ALTER COLUMN receive_amount SET NOT NULL;
  
  ALTER TABLE sec_fxforward ADD CONSTRAINT sec_fk_fxforward_pay2currency FOREIGN KEY (pay_currency_id) REFERENCES sec_currency (id);
  ALTER TABLE sec_fxforward ADD CONSTRAINT sec_fk_fxforward_rcv2currency FOREIGN KEY (receive_currency_id) REFERENCES sec_currency (id);
  
  -- remove existing reference to underlying columns
  ALTER TABLE sec_fxforward DROP COLUMN underlying_scheme;
  ALTER TABLE sec_fxforward DROP COLUMN underlying_identifier;
  
  -- NON-DELIVERABLE FX FORWARDS ==================================
  -- Pay Currency
  ALTER TABLE sec_nondeliverablefxforward ADD COLUMN pay_currency_id BIGINT;
  
  UPDATE sec_nondeliverablefxforward SET pay_currency_id=(SELECT pay_currency_id FROM sec_fx
                                            LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                            LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                            WHERE keys.key_scheme=underlying_scheme AND
                                                  keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_nondeliverablefxforward ALTER COLUMN pay_currency_id SET NOT NULL;
  
  -- Receive Currency
  ALTER TABLE sec_nondeliverablefxforward ADD COLUMN receive_currency_id BIGINT;
  
  UPDATE sec_nondeliverablefxforward SET receive_currency_id=(SELECT receive_currency_id FROM sec_fx
                                                LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                                LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                                WHERE keys.key_scheme=underlying_scheme AND
                                                      keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_nondeliverablefxforward ALTER COLUMN receive_currency_id SET NOT NULL;
  
  -- Pay Amount
  ALTER TABLE sec_nondeliverablefxforward ADD COLUMN pay_amount DOUBLE PRECISION;
  
  UPDATE sec_nondeliverablefxforward SET pay_amount=(SELECT pay_amount FROM sec_fx
                                       LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                       LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                       WHERE keys.key_scheme=underlying_scheme AND
                                             keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_nondeliverablefxforward ALTER COLUMN pay_amount SET NOT NULL;
  -- Receive Amount
  ALTER TABLE sec_nondeliverablefxforward ADD COLUMN receive_amount DOUBLE PRECISION;
  
  UPDATE sec_nondeliverablefxforward SET receive_amount=(SELECT receive_amount FROM sec_fx
                                           LEFT JOIN sec_security2idkey AS idkey ON sec_fx.security_id=idkey.security_id
                                           LEFT JOIN sec_idkey AS keys ON idkey.idkey_id=keys.id
                                           WHERE keys.key_scheme=underlying_scheme AND
                                                 keys.key_value=underlying_identifier);
  
  ALTER TABLE sec_nondeliverablefxforward ALTER COLUMN receive_amount SET NOT NULL;
  
  --ALTER TABLE sec_nondeliverablefxforward ADD COLUMN is_delivery_in_receive_currency BOOLEAN DEFAULT TRUE NOT NULL;
  
  ALTER TABLE sec_nondeliverablefxforward ADD CONSTRAINT sec_fk_nondeliverablefxforward_pay2currency FOREIGN KEY (pay_currency_id) REFERENCES sec_currency (id);
  ALTER TABLE sec_nondeliverablefxforward ADD CONSTRAINT sec_fk_nondeliverablefxforward_rcv2currency FOREIGN KEY (receive_currency_id) REFERENCES sec_currency (id);
  
  -- remove existing reference to underlying columns
  ALTER TABLE sec_nondeliverablefxforward DROP COLUMN underlying_scheme;
  ALTER TABLE sec_nondeliverablefxforward DROP COLUMN underlying_identifier;
      
  -- remove any ids 
  DELETE FROM sec_security2idkey WHERE security_id IN (SELECT security_id FROM sec_fx);
  -- get rid of the fx table completely
  DROP TABLE sec_fx;
  DELETE FROM sec_security WHERE sec_type='FX';
COMMIT;