
-- create-db-security.sql: Security Master

-- design has one document
--  security and associated identity key
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE sec_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO sec_schema_version (version_key, version_value) VALUES ('schema_patch', '68');

CREATE TABLE sec_hibernate_sequence (
  next_val numeric(19,0)
)

-- CREATE SEQUENCE sec_security_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE TABLE sec_security_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)

-- CREATE SEQUENCE sec_idkey_seq
--     START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as BIGINT" required by Derby/HSQL, not accepted by Postgresql
CREATE TABLE sec_idkey_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)

CREATE TABLE sec_security (
    id BIGINT NOT NULL,
    oid BIGINT NOT NULL,
    ver_from_instant DATETIME2(6) NOT NULL,
    ver_to_instant DATETIME2(6) NOT NULL,
    corr_from_instant DATETIME2(6) NOT NULL,
    corr_to_instant DATETIME2(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    uname AS UPPER(name),
    sec_type VARCHAR(255) NOT NULL,
    usec_type AS UPPER(sec_type),
    detail_type char NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_sec2sec FOREIGN KEY (oid) REFERENCES sec_security (id),
    CONSTRAINT sec_chk_sec_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT sec_chk_sec_corr_order CHECK (corr_from_instant <= corr_to_instant),
    CONSTRAINT sec_chk_detail_type CHECK (detail_type IN ('D', 'M', 'R'))
);
CREATE INDEX ix_sec_security_oid ON sec_security(oid);
CREATE INDEX ix_sec_security_ver_from_instant ON sec_security(ver_from_instant);
CREATE INDEX ix_sec_security_ver_to_instant ON sec_security(ver_to_instant);
CREATE INDEX ix_sec_security_corr_from_instant ON sec_security(corr_from_instant);
CREATE INDEX ix_sec_security_corr_to_instant ON sec_security(corr_to_instant);
CREATE INDEX ix_sec_security_name ON sec_security(name);
CREATE INDEX ix_sec_security_nameu ON sec_security(uname);
CREATE INDEX ix_sec_security_sec_type ON sec_security(sec_type);
CREATE INDEX ix_sec_security_sec_typeu ON sec_security(usec_type);

CREATE TABLE sec_idkey (
    id BIGINT NOT NULL,
    key_scheme VARCHAR(255) NOT NULL,
    key_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE sec_security2idkey (
    security_id BIGINT NOT NULL,
    idkey_id BIGINT NOT NULL,
    PRIMARY KEY (security_id, idkey_id),
    CONSTRAINT sec_fk_secidkey2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_secidkey2idkey FOREIGN KEY (idkey_id) REFERENCES sec_idkey (id)
);
CREATE INDEX ix_sec_sec2idkey_idkey ON sec_security2idkey(idkey_id);
-- sec_security_idkey is fully dependent of sec_security

-- Hibernate controlled tables
CREATE TABLE sec_currency (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_cashrate (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_unit (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_exchange (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE sec_gics (
    id BIGINT NOT NULL,
    name VARCHAR(8) NOT NULL UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE sec_equity (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    shortName VARCHAR(255),
    exchange_id BIGINT NOT NULL,
    companyName VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    gicscode_id BIGINT,
    preferred BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equity2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equity2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
    CONSTRAINT sec_fk_equity2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange(id),
    CONSTRAINT sec_fk_equity2gics FOREIGN KEY (gicscode_id) REFERENCES sec_gics(id)
);
CREATE INDEX ix_sec_equity_security_id ON sec_equity(security_id);

CREATE TABLE sec_equityindexoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT,
    pointValue double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindexoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityindexoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_equityindexoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_equityoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT,
    pointValue double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_equityoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equityoption_security_id ON sec_equityoption(security_id);

CREATE TABLE sec_equitybarrieroption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT,
    pointValue double precision,
	  barrier_type VARCHAR(32) NOT NULL,
    barrier_direction VARCHAR(32) NOT NULL,
    barrier_level double precision NOT NULL,
    monitoring_type VARCHAR(32) NOT NULL,
    sampling_frequency VARCHAR(32),
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equitybarrieroption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equitybarrieroption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_equitybarrieroption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equitybarrieroption_security_id ON sec_equitybarrieroption(security_id);


CREATE TABLE sec_fxoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id BIGINT NOT NULL,
    call_currency_id BIGINT NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    is_long BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxoption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxoption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);
CREATE INDEX ix_sec_fxoption_security_id ON sec_fxoption(security_id);

CREATE TABLE sec_nondeliverablefxoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id BIGINT NOT NULL,
    call_currency_id BIGINT NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    is_long BIT NOT NULL,
	  is_delivery_in_call_currency BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);
CREATE INDEX ix_sec_nondeliverablefxoption_security_id ON sec_nondeliverablefxoption(security_id);

CREATE TABLE sec_fxdigitaloption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id BIGINT NOT NULL,
    call_currency_id BIGINT NOT NULL,
    payment_currency_id BIGINT NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    is_long BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxdigitaloption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxdigitaloption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxdigitaloption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id)
);

CREATE INDEX ix_sec_fxdigitaloption_security_id ON sec_fxdigitaloption(security_id);

CREATE TABLE sec_ndffxdigitaloption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id BIGINT NOT NULL,
    call_currency_id BIGINT NOT NULL,
    payment_currency_id BIGINT NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    is_long BIT NOT NULL,
    is_delivery_in_call_currency BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_ndffxdigitaloption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_ndffxdigitaloption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_ndffxdigitaloption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_ndffxdigitaloption2paymentcurrency FOREIGN KEY (payment_currency_id) REFERENCES sec_currency (id)
);

CREATE INDEX ix_sec_ndffxdigitaloption_security_id ON sec_ndffxdigitaloption(security_id);

CREATE TABLE sec_swaption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    cash_settled BIT NOT NULL,
    is_long BIT NOT NULL,
    is_payer BIT NOT NULL,
    currency_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32),
    settlement_date DATETIME2(6),
    settlement_zone VARCHAR(50),
    notional double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_swaption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
    CONSTRAINT sec_fk_swaption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_irfutureoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    margined BIT NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_irfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_irfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_irfutureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_commodityfutureoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    trading_exchange_id bigint NOT NULL,
    settlement_exchange_id bigint NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_commodityfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_commodityfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_commodityfutureoption2trading_exchange FOREIGN KEY (trading_exchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_commodityfutureoption2settlement_exchange FOREIGN KEY (settlement_exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_bondfutureoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    trading_exchange_id bigint NOT NULL,
    settlement_exchange_id bigint NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bondfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_bondfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_bondfutureoption2trading_exchange FOREIGN KEY (trading_exchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_bondfutureoption2settlement_exchange FOREIGN KEY (settlement_exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_equity_index_dividend_futureoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    margined BIT NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equity_index_dividend_futureoption_security_id ON sec_equity_index_dividend_futureoption(security_id);

CREATE TABLE sec_fxbarrieroption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id BIGINT NOT NULL,
    call_currency_id BIGINT NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    barrier_type VARCHAR(32) NOT NULL,
    barrier_direction VARCHAR(32) NOT NULL,
    barrier_level double precision NOT NULL,
    monitoring_type VARCHAR(32) NOT NULL,
    sampling_frequency VARCHAR(32),
    is_long BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxbarrieroption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxbarrieroption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxbarrieroption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_frequency (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_daycount (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_businessdayconvention (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_issuertype (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_market (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_yieldconvention (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_guaranteetype (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_coupontype (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_stubtype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_bond (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    bond_type VARCHAR(32) NOT NULL,
    issuername VARCHAR(255) NOT NULL,
    issuertype_id BIGINT NOT NULL,
    issuerdomicile VARCHAR(255) NOT NULL,
    market_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    yieldconvention_id BIGINT NOT NULL,
    guaranteetype_id BIGINT,
    maturity_date DATETIME2(6) NOT NULL,
    maturity_zone VARCHAR(50) NOT NULL,
    maturity_accuracy smallint NOT NULL,
    coupontype_id BIGINT NOT NULL,
    couponrate double precision NOT NULL,
    couponfrequency_id BIGINT NOT NULL,
    daycountconvention_id BIGINT NOT NULL,
    businessdayconvention_id BIGINT,
    announcement_date DATETIME2(6),
    announcement_zone VARCHAR(50),
    interestaccrual_date DATETIME2(6),
    interestaccrual_zone VARCHAR(50),
    settlement_date DATETIME2(6),
    settlement_zone VARCHAR(50),
    firstcoupon_date DATETIME2(6),
    firstcoupon_zone VARCHAR(50),
    issuanceprice double precision,
    totalamountissued double precision NOT NULL,
    minimumamount double precision NOT NULL,
    minimumincrement double precision NOT NULL,
    paramount double precision NOT NULL,
    redemptionvalue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bond2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_bond2issuertype FOREIGN KEY (issuertype_id) REFERENCES sec_issuertype (id),
    CONSTRAINT sec_fk_bond2market FOREIGN KEY (market_id) REFERENCES sec_market (id),
    CONSTRAINT sec_fk_bond2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_bond2yieldconvention FOREIGN KEY (yieldconvention_id) REFERENCES sec_yieldconvention (id),
    CONSTRAINT sec_fk_bond2guaranteetype FOREIGN KEY (guaranteetype_id) REFERENCES sec_guaranteetype (id),
    CONSTRAINT sec_fk_bond2coupontype FOREIGN KEY (coupontype_id) REFERENCES sec_coupontype (id),
    CONSTRAINT sec_fk_bond2frequency FOREIGN KEY (couponfrequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_bond2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
    CONSTRAINT sec_fk_bond2businessdayconvention FOREIGN KEY (businessdayconvention_id) REFERENCES sec_businessdayconvention (id)
);
CREATE INDEX ix_sec_bond_security_id ON sec_bond(security_id);

CREATE TABLE sec_contract_category (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE sec_future (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    future_type VARCHAR(32) NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    tradingexchange_id BIGINT NOT NULL,
    settlementexchange_id BIGINT NOT NULL,
    currency1_id BIGINT,
    currency2_id BIGINT,
    currency3_id BIGINT,
    unitname_id BIGINT,
    unitnumber double precision,
    unit_amount double precision,
    underlying_scheme VARCHAR(255),
    underlying_identifier VARCHAR(255),
    bondFutureFirstDeliveryDate DATETIME2(6),
    bondFutureFirstDeliveryDate_zone VARCHAR(50),
    bondFutureLastDeliveryDate DATETIME2(6),
    bondFutureLastDeliveryDate_zone VARCHAR(50),
    contract_category_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_future2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_future2exchange1 FOREIGN KEY (tradingexchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_future2exchange2 FOREIGN KEY (settlementexchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_future2currency1 FOREIGN KEY (currency1_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2currency2 FOREIGN KEY (currency2_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2currency3 FOREIGN KEY (currency3_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2unit FOREIGN KEY (unitname_id) REFERENCES sec_unit (id),
    CONSTRAINT sec_fk_future2contract_category FOREIGN KEY (contract_category_id) REFERENCES sec_contract_category (id)
);


CREATE INDEX ix_sec_future_security_id ON sec_future(security_id);

CREATE TABLE sec_futurebundle (
    id BIGINT NOT NULL,
    future_id BIGINT NOT NULL,
    startDate DATETIME2(6),
    endDate DATETIME2(6),
    conversionFactor double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_futurebundle2future FOREIGN KEY (future_id) REFERENCES sec_future (id)
);

CREATE TABLE sec_futurebundleidentifier (
    bundle_id BIGINT NOT NULL,
    scheme VARCHAR(255) NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_futurebundleidentifier2futurebundle FOREIGN KEY (bundle_id) REFERENCES sec_futurebundle (id)
);

CREATE TABLE sec_commodity_forward (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    forward_type VARCHAR(32) NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    currency_id BIGINT,
    unitname_id BIGINT,
    unitnumber double precision,
    unit_amount double precision,
    underlying_scheme VARCHAR(255),
    underlying_identifier VARCHAR(255),
    contract_category_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_commodity_forward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_commodity_forward2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_commodity_forward2unit FOREIGN KEY (unitname_id) REFERENCES sec_unit (id),
    CONSTRAINT sec_fk_commodity_forward2contract_category FOREIGN KEY (contract_category_id) REFERENCES sec_contract_category (id)
);

CREATE INDEX ix_sec_commodity_forward_security_id ON sec_commodity_forward(security_id);


CREATE TABLE sec_cash (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    region_scheme VARCHAR(255) NOT NULL,
    region_identifier VARCHAR(255) NOT NULL,
    start_date DATETIME2(6) NOT NULL,
    start_zone VARCHAR(50) NOT NULL,
    maturity_date DATETIME2(6) NOT NULL,
    maturity_zone VARCHAR(50) NOT NULL,
    daycount_id BIGINT NOT NULL,
    rate double precision NOT NULL,
    amount double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cash2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cash2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_cash2daycount FOREIGN KEY (daycount_id) REFERENCES sec_daycount (id)
);

CREATE TABLE sec_fra (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    region_scheme VARCHAR(255) NOT NULL,
    region_identifier VARCHAR(255) NOT NULL,
    start_date DATETIME2(6) NOT NULL,
    start_zone VARCHAR(50) NOT NULL,
    end_date DATETIME2(6) NOT NULL,
    end_zone VARCHAR(50) NOT NULL,
    rate double precision NOT NULL,
    amount double precision NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    fixing_date DATETIME2(6) NOT NULL,
    fixing_zone VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fra2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fra2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_tenor (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_swap (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    swaptype VARCHAR(32) NOT NULL,
    trade_date DATETIME2(6) NOT NULL,
    trade_zone VARCHAR(50) NOT NULL,
    effective_date DATETIME2(6) NOT NULL,
    effective_zone VARCHAR(50) NOT NULL,
    maturity_date DATETIME2(6) NOT NULL,
    maturity_zone VARCHAR(50) NOT NULL,
    forwardstart_date DATETIME2(6),
    forwardstart_zone VARCHAR(50),
    counterparty VARCHAR(255) NOT NULL,
    exchange_initial_notional BIT NOT NULL,
    exchange_final_notional BIT NOT NULL,
    maturity_tenor_id bigint,
    pay_legtype VARCHAR(32) NOT NULL,
    pay_daycount_id BIGINT NOT NULL,
    pay_frequency_id BIGINT NOT NULL,
    pay_regionscheme VARCHAR(255) NOT NULL,
    pay_regionid VARCHAR(255) NOT NULL,
    pay_businessdayconvention_id BIGINT NOT NULL,
    pay_notionaltype VARCHAR(32) NOT NULL,
    pay_notionalcurrency_id BIGINT,
    pay_notionalamount double precision,
    pay_notionalscheme VARCHAR(255),
    pay_notionalid VARCHAR(255),
    pay_rate double precision,
    pay_iseom BIT NOT NULL,
    pay_spread double precision,
    pay_rateidentifierscheme VARCHAR(255),
    pay_rateidentifierid VARCHAR(255),
    pay_floating_rate_type VARCHAR(32),
    pay_settlement_days INTEGER,
    pay_gearing DOUBLE precision,
    pay_offset_fixing_id BIGINT,
    pay_strike double precision,
    pay_variance_swap_type VARCHAR(32),
    pay_underlying_identifier VARCHAR(255),
    pay_underlying_scheme VARCHAR(255),
    pay_monitoring_frequency_id BIGINT,
    pay_annualization_factor double precision,
    pay_conventional_indexation_lag INTEGER,
    pay_actual_indexation_lag INTEGER,
    pay_index_interpolation_method varchar(32),
    receive_legtype VARCHAR(32) NOT NULL,
    receive_daycount_id BIGINT NOT NULL,
    receive_frequency_id BIGINT NOT NULL,
    receive_regionscheme VARCHAR(255) NOT NULL,
    receive_regionid VARCHAR(255) NOT NULL,
    receive_businessdayconvention_id BIGINT NOT NULL,
    receive_notionaltype VARCHAR(32) NOT NULL,
    receive_notionalcurrency_id BIGINT,
    receive_notionalamount double precision,
    receive_notionalscheme VARCHAR(255),
    receive_notionalid VARCHAR(255),
    receive_rate double precision,
    receive_iseom BIT NOT NULL,
    receive_spread double precision,
    receive_rateidentifierscheme VARCHAR(255),
    receive_rateidentifierid VARCHAR(255),
    receive_floating_rate_type VARCHAR(32),
    receive_settlement_days INTEGER,
    receive_gearing DOUBLE precision,
    receive_offset_fixing_id BIGINT,
    receive_strike double precision,
    receive_variance_swap_type VARCHAR(32),
    receive_underlying_identifier VARCHAR(255),
    receive_underlying_scheme VARCHAR(255),
    receive_monitoring_frequency_id BIGINT,
    receive_annualization_factor double precision,
    receive_conventional_indexation_lag INTEGER,
    receive_actual_indexation_lag INTEGER,
    receive_index_interpolation_method varchar(32),
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_swap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_payfreq2frequency FOREIGN KEY (pay_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_receivefreq2frequency FOREIGN KEY (receive_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_payoffset2frequency FOREIGN KEY (pay_offset_fixing_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_recvoffset2frequency FOREIGN KEY (receive_offset_fixing_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_paymonitorfreq2frequency FOREIGN KEY (pay_monitoring_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_recvmonitorfreq2frequency FOREIGN KEY (receive_monitoring_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_sec_swapmaturitytenor2tenor FOREIGN KEY (maturity_tenor_id) REFERENCES sec_tenor (id)
);
CREATE INDEX ix_sec_swap_security_id ON sec_swap(security_id);

CREATE TABLE sec_raw (
    security_id BIGINT NOT NULL,
    raw_data IMAGE NOT NULL,
    CONSTRAINT sec_fk_raw2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_fxforward (
  id BIGINT NOT NULL,
  security_id BIGINT NOT NULL,
  region_scheme VARCHAR(255) NOT NULL,
  region_identifier VARCHAR(255) NOT NULL,
  pay_currency_id BIGINT NOT NULL,
  receive_currency_id BIGINT NOT NULL,
  pay_amount DOUBLE PRECISION NOT NULL,
  receive_amount DOUBLE PRECISION NOT NULL,
  forward_date DATETIME2(6) NOT NULL,
  forward_zone VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_fxforward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_fxforward_pay2currency FOREIGN KEY (pay_currency_id) REFERENCES sec_currency (id),
  CONSTRAINT sec_fk_fxforward_rcv2currency FOREIGN KEY (receive_currency_id) REFERENCES sec_currency (id)

);
CREATE INDEX ix_sec_fxforward_security_id ON sec_fxforward(security_id);

CREATE TABLE sec_nondeliverablefxforward (
  id BIGINT NOT NULL,
  security_id BIGINT NOT NULL,
  region_scheme VARCHAR(255) NOT NULL,
  region_identifier VARCHAR(255) NOT NULL,
  pay_currency_id BIGINT NOT NULL,
  receive_currency_id BIGINT NOT NULL,
  pay_amount DOUBLE PRECISION NOT NULL,
  receive_amount DOUBLE PRECISION NOT NULL,
  forward_date DATETIME2(6) NOT NULL,
  forward_zone VARCHAR(50) NOT NULL,
  is_delivery_in_receive_currency BIT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_nondeliverablefxforward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_nondeliverablefxforward_pay2currency FOREIGN KEY (pay_currency_id) REFERENCES sec_currency (id),
  CONSTRAINT sec_fk_nondeliverablefxforward_rcv2currency FOREIGN KEY (receive_currency_id) REFERENCES sec_currency (id)
);
CREATE INDEX ix_sec_nondeliverablefxforward_security_id ON sec_nondeliverablefxforward(security_id);

CREATE TABLE sec_capfloor (
  id BIGINT NOT NULL,
  security_id BIGINT NOT NULL,
  currency_id BIGINT NOT NULL,
  daycountconvention_id BIGINT NOT NULL,
  frequency_id BIGINT NOT NULL,
  is_cap BIT NOT NULL,
  is_ibor BIT NOT NULL,
  is_payer BIT NOT NULL,
  maturity_date DATETIME2(6) NOT NULL,
  maturity_zone VARCHAR(50) NOT NULL,
  notional double precision NOT NULL,
  start_date DATETIME2(6) NOT NULL,
  start_zone VARCHAR(50) NOT NULL,
  strike double precision NOT NULL,
  underlying_scheme VARCHAR(255) NOT NULL,
  underlying_identifier VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_capfloor2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_capfloor2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_capfloor2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_capfloor2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id)
);

CREATE TABLE  sec_capfloorcmsspread (
  id BIGINT NOT NULL,
  security_id BIGINT NOT NULL,
  currency_id BIGINT NOT NULL,
  daycountconvention_id BIGINT NOT NULL,
  frequency_id BIGINT NOT NULL,
  is_cap BIT NOT NULL,
  is_payer BIT NOT NULL,
  long_scheme VARCHAR(255) NOT NULL,
  long_identifier VARCHAR(255) NOT NULL,
  maturity_date DATETIME2(6) NOT NULL,
  maturity_zone VARCHAR(50) NOT NULL,
  notional double precision NOT NULL,
  short_scheme VARCHAR(255) NOT NULL,
  short_identifier VARCHAR(255) NOT NULL,
  start_date DATETIME2(6) NOT NULL,
  start_zone VARCHAR(50) NOT NULL,
  strike double precision NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_capfloorcmsspread2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_capfloorcmsspread2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_capfloorcmsspread2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_capfloorcmsspread2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id)
);

CREATE TABLE  sec_equity_variance_swap (
  id BIGINT NOT NULL,
  security_id BIGINT NOT NULL,
  annualization_factor double precision NOT NULL,
  currency_id BIGINT NOT NULL,
  first_observation_date DATETIME2(6) NOT NULL,
  first_observation_zone VARCHAR(50) NOT NULL,
  last_observation_date DATETIME2(6) NOT NULL,
  last_observation_zone VARCHAR(50) NOT NULL,
  notional double precision NOT NULL,
  observation_frequency_id BIGINT NOT NULL,
  parameterised_as_variance BIT NOT NULL,
  region_scheme VARCHAR(255) NOT NULL,
  region_id VARCHAR(255) NOT NULL,
  settlement_date DATETIME2(6) NOT NULL,
  settlement_zone VARCHAR(50) NOT NULL,
  spot_scheme VARCHAR(255) NOT NULL,
  spot_id VARCHAR(255) NOT NULL,
  strike double precision NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_equityvarianceswap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_equityvarianceswap2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_equityvarianceswap2frequency FOREIGN KEY (observation_frequency_id) REFERENCES sec_frequency (id)
);

-- CREATE SEQUENCE sec_security_attr_seq
--     start with 1000 increment by 1 no cycle;
CREATE TABLE sec_security_attr_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
)

CREATE TABLE sec_security_attribute (
    id BIGINT not null,
    security_id BIGINT not null,
    security_oid BIGINT not null,
    attr_key VARCHAR(255) not null,
    attr_value VARCHAR(255) not null,
    primary key (id),
    constraint sec_fk_securityattr2security foreign key (security_id) references sec_security (id),
    constraint sec_chk_uq_security_attribute unique (security_id, attr_key, attr_value)
);
-- security_oid is an optimization
-- sec_security_attribute is fully dependent of sec_security
CREATE INDEX ix_sec_security_attr_security_oid ON sec_security_attribute(security_oid);
CREATE INDEX ix_sec_security_attr_key ON sec_security_attribute(attr_key);

CREATE TABLE sec_cds (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  notional double precision NOT NULL,
  recovery_rate double precision NOT NULL,
  spread double precision NOT NULL,
  currency_id bigint NOT NULL,
  maturity_date DATETIME2(6) NOT NULL,
  maturity_date_zone varchar(50) NOT NULL,
  start_date DATETIME2(6) NOT NULL,
  start_date_zone varchar(50) NOT NULL,
  premium_frequency_id bigint NOT NULL,
  daycountconvention_id bigint NOT NULL,
  businessdayconvention_id bigint NOT NULL,
  stubtype_id bigint NOT NULL,
  settlement_days int NOT NULL,
  underlying_issuer varchar(255) NOT NULL,
  underlying_currency_id bigint NOT NULL,
  underlying_seniority varchar(255) NOT NULL,
  restructuring_clause varchar(255) NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT sec_fk_cds2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_cds2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
  CONSTRAINT sec_fk_cds2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_cds2businessdayconvention FOREIGN KEY (businessdayconvention_id) REFERENCES sec_businessdayconvention (id),
  CONSTRAINT sec_fk_cds2frequency FOREIGN KEY (premium_frequency_id) REFERENCES sec_frequency (id),
  CONSTRAINT sec_fk_cds2stubtype FOREIGN KEY (stubtype_id) REFERENCES sec_stubtype (id),
  CONSTRAINT sec_fk_cds_underlying2currency FOREIGN KEY (underlying_currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_debt_seniority (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

 CREATE TABLE  sec_restructuring_clause (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
 );

  CREATE TABLE sec_credit_default_swap (
   id bigint NOT NULL,
   security_id bigint NOT NULL,
   cds_type varchar(255) NOT NULL,
   buy bit NOT NULL,
   buyer_scheme varchar(255) NOT NULL,
   buyer_identifier varchar(255) NOT NULL,
   seller_scheme varchar(255) NOT NULL,
   seller_identifier varchar(255) NOT NULL,
   entity_scheme varchar(255) NOT NULL,
   entity_identifier varchar(255) NOT NULL,
   debt_seniority_id bigint NOT NULL,
   restructuring_clause_id bigint NOT NULL,
   region_scheme varchar(255) NOT NULL,
   region_identifier varchar(255) NOT NULL,
   start_date DATETIME2(6) NOT NULL,
   start_date_zone varchar(50) NOT NULL,
   effective_date DATETIME2(6) NOT NULL,
   effective_date_zone varchar(50) NOT NULL,
   maturity_date DATETIME2(6) NOT NULL,
   maturity_date_zone varchar(50) NOT NULL,
   stub_type_id bigint NOT NULL,
   frequency_id bigint NOT NULL,
   daycount_convention_id bigint NOT NULL,
   businessday_convention_id bigint NOT NULL,
   imm_adjust_maturity_date bit NOT NULL,
   adjust_effective_date bit NOT NULL,
   adjust_maturity_date bit NOT NULL,
   notional_type varchar(32) NOT NULL,
   notional_currency_id bigint NOT NULL,
   notional_amount double precision,
   notional_scheme varchar(255),
   notional_id varchar(255),
   recovery_rate double precision,
   include_accrued_premium bit NOT NULL,
   protection_start bit NOT NULL,
   legacy_par_spread double precision,
   std_quoted_spread double precision,
   std_upfrontamt_notional_type varchar(32),
   std_upfrontamt_notional_currency_id bigint,
   std_upfrontamt_notional_amount double precision,
   std_upfrontamt_notional_scheme varchar(255),
   std_upfrontamt_notional_id varchar(255),
   stdvanilla_coupon double precision,
   stdvanilla_cashsettlement_date DATETIME2(6),
   stdvanilla_ashsettlement_date_zone varchar(50),
   stdvanilla_adjust_cashsettlement_date bit,

   PRIMARY KEY (id),
   CONSTRAINT sec_fk_creditdefaultswap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
   CONSTRAINT sec_fk_creditdefaultswap2debtseniority FOREIGN KEY (debt_seniority_id) REFERENCES sec_debt_seniority(id),
   CONSTRAINT sec_fk_creditdefaultswap2restructuringclause FOREIGN KEY (restructuring_clause_id) REFERENCES sec_restructuring_clause(id),
   CONSTRAINT sec_fk_creditdefaultswap2stubtype FOREIGN KEY (stub_type_id) REFERENCES sec_stubtype (id),
   CONSTRAINT sec_fk_creditdefaultswap2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id),
   CONSTRAINT sec_fk_creditdefaultswap2daycount FOREIGN KEY (daycount_convention_id) REFERENCES sec_daycount (id),
   CONSTRAINT sec_fk_creditdefaultswap2businessdayconvention FOREIGN KEY (businessday_convention_id) REFERENCES sec_businessdayconvention (id),
   CONSTRAINT sec_fk_creditdefaultswap2currency FOREIGN KEY (notional_currency_id) REFERENCES sec_currency (id)
 );
 CREATE INDEX ix_sec_creditdefaultswap_security_id ON sec_credit_default_swap(security_id);

 CREATE TABLE sec_cashflow (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    amount double precision NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cashflow2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cashflow2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
 );

CREATE TABLE sec_equity_index_futureoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    margined BIT NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_equity_index_futureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_equity_index_futureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_equity_index_futureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equity_index_futureoption_security_id ON sec_equity_index_futureoption(security_id);

CREATE TABLE sec_fxfutureoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    trading_exchange_id bigint NOT NULL,
    settlement_exchange_id bigint NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxfutureoption2trading_exchange FOREIGN KEY (trading_exchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_fxfutureoption2settlement_exchange FOREIGN KEY (settlement_exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_cdsid_family (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
);

CREATE TABLE sec_credit_default_swap_index_definition (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    version varchar(255) NOT NULL,
    series varchar(255) NOT NULL,
    family_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    recovery_rate double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_credit_index_defn2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_credit_index_defn2family FOREIGN KEY (family_id) REFERENCES sec_cdsid_family (id),
    CONSTRAINT sec_credit_index_defn2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_cdsid_tenor (
    cdsid_id bigint NOT NULL,
    tenor_id bigint NOT NULL,
    PRIMARY KEY (cdsid_id, tenor_id),
    CONSTRAINT sec_fk_sec_cdsid_tenor2cdsid FOREIGN KEY (cdsid_id) REFERENCES sec_credit_default_swap_index_definition (id),
    CONSTRAINT sec_fk_sec_cdsid_tenor2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
);

CREATE TABLE sec_cdsid_component (
    cdsid_id bigint NOT NULL,
    obligor_scheme varchar(255) NOT NULL,
    obligor_identifier varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    weight double precision NOT NULL,
    bond_scheme varchar(255),
    bond_identifier varchar(255),
    PRIMARY KEY (cdsid_id, obligor_scheme, obligor_identifier),
    CONSTRAINT sec_fk_cdsid_component2cdsid FOREIGN KEY (cdsid_id) REFERENCES sec_credit_default_swap_index_definition (id)
);

CREATE TABLE sec_credit_default_swap_option (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    buy bit NOT NULL,
    buyer_scheme varchar(255) NOT NULL,
    buyer_identifier varchar(255) NOT NULL,
    seller_scheme varchar(255) NOT NULL,
    seller_identifier varchar(255) NOT NULL,
    start_date datetime2(6) NOT NULL,
    start_date_zone varchar(50) NOT NULL,
    maturity_date datetime2(6) NOT NULL,
    maturity_date_zone varchar(50) NOT NULL,
    currency_id bigint NOT NULL,
    notional double precision NOT NULL,
    strike double precision NOT NULL,
    knock_out bit NOT NULL,
    payer bit NOT NULL,
    exercise_type varchar(32) NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT sec_fk_creditdefaultswapoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_creditdefaultswapoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id)
);

CREATE TABLE sec_credit_default_swap_index (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    buy bit NOT NULL,
    buyer_scheme varchar(255) NOT NULL,
    buyer_identifier varchar(255) NOT NULL,
    seller_scheme varchar(255) NOT NULL,
    seller_identifier varchar(255) NOT NULL,
    index_scheme varchar(255) NOT NULL,
    index_identifier varchar(255) NOT NULL,
    start_date DATETIME2(6) NOT NULL,
    start_date_zone varchar(50) NOT NULL,
    effective_date DATETIME2(6) NOT NULL,
    effective_date_zone varchar(50) NOT NULL,
    maturity_date DATETIME2(6) NOT NULL,
    maturity_date_zone varchar(50) NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_date_zone varchar(50) NOT NULL,
    stub_type_id bigint NOT NULL,
    frequency_id bigint NOT NULL,
    daycount_convention_id bigint NOT NULL,
    businessday_convention_id bigint NOT NULL,
    imm_adjust_maturity_date bit NOT NULL,
    adjust_effective_date bit NOT NULL,
    adjust_maturity_date bit NOT NULL,
    adjust_settlement_date bit NOT NULL,
    notional_type varchar(32) NOT NULL,
    notional_currency_id bigint NOT NULL,
    notional_amount double precision,
    notional_scheme varchar(255),
    notional_id varchar(255),
    include_accrued_premium bit NOT NULL,
    protection_start bit NOT NULL,
    upfrontamt_notional_type varchar(32),
    upfrontamt_notional_currency_id bigint,
    upfrontamt_notional_amount double precision,
    upfrontamt_notional_scheme varchar(255),
    upfrontamt_notional_id varchar(255),
    index_coupon double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cdsindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cdsindex2stubtype FOREIGN KEY (stub_type_id) REFERENCES sec_stubtype (id),
    CONSTRAINT sec_fk_cdsindex2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_cdsindex2daycount FOREIGN KEY (daycount_convention_id) REFERENCES sec_daycount (id),
    CONSTRAINT sec_fk_cdsindex2businessdayconvention FOREIGN KEY (businessday_convention_id) REFERENCES sec_businessdayconvention (id),
    CONSTRAINT sec_fk_cdsindex2currency FOREIGN KEY (notional_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_cdsindex2upfrontamtcurrency FOREIGN KEY (upfrontamt_notional_currency_id) REFERENCES sec_currency (id)

);

CREATE INDEX ix_sec_cdsindex_security_id ON sec_credit_default_swap_index(security_id);

CREATE TABLE sec_indexweightingtype (
    id bigint NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- Indices
CREATE TABLE sec_iborindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    tenor_id bigint NOT NULL,
    convention_scheme VARCHAR(255) NOT NULL,
    convention_identifier VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_iborindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_iborindex2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
);

CREATE TABLE sec_overnightindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    convention_scheme VARCHAR(255) NOT NULL,
    convention_identifier VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_overnightindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_bondindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    indexweightingtype_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bondindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_bondindex2indexweightingtype FOREIGN KEY (indexweightingtype_id) REFERENCES sec_indexweightingtype (id)
);

CREATE TABLE sec_bondindexcomponent (
    id bigint NOT NULL,
    position bigint NOT NULL,
    bondindex_id bigint NOT NULL,
    weight decimal(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bondindexcomponent2bondindex FOREIGN KEY (bondindex_id) REFERENCES sec_bondindex (id)
);

CREATE TABLE sec_bondindexcomponentidentifier (
    bundle_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_bondindexcomponentidentifier2bondindexcomponent FOREIGN KEY (bundle_id) REFERENCES sec_bondindexcomponent (id)
);

CREATE TABLE sec_equityindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    indexweightingtype_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityindex2indexweightingtype FOREIGN KEY (indexweightingtype_id) REFERENCES sec_indexweightingtype (id)
);

CREATE TABLE sec_equityindexcomponent (
    id bigint NOT NULL,
    position bigint NOT NULL,
    equityindex_id bigint NOT NULL,
    weight decimal(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindexcomponent2equityindex FOREIGN KEY (equityindex_id) REFERENCES sec_equityindex (id)
);

CREATE TABLE sec_equityindexcomponentidentifier (
    bundle_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_equityindexcomponentidentifier2equityindexcomponent FOREIGN KEY (bundle_id) REFERENCES sec_equityindexcomponent (id)
);

CREATE TABLE sec_indexfamily (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_indexfamily2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_indexfamilyentry (
    indexfamily_id bigint NOT NULL,
    tenor_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (indexfamily_id, tenor_id),
    CONSTRAINT sec_fk_indexfamilyentry2indexfamily FOREIGN KEY (indexfamily_id) REFERENCES sec_indexfamily (id),
    CONSTRAINT sec_fk_indexfamilyentry2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
);