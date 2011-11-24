
-- create-db-security.sql: Security Master

-- design has one document
--  security and associated identity key
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE sec_security_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE sec_idkey_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE sec_security (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    sec_type varchar(255) NOT NULL,
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
CREATE INDEX ix_sec_security_nameu ON sec_security(UPPER(name));
CREATE INDEX ix_sec_security_sec_type ON sec_security(sec_type);
CREATE INDEX ix_sec_security_sec_typeu ON sec_security(UPPER(sec_type));

CREATE TABLE sec_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE sec_security2idkey (
    security_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (security_id, idkey_id),
    CONSTRAINT sec_fk_secidkey2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_secidkey2idkey FOREIGN KEY (idkey_id) REFERENCES sec_idkey (id)
);
CREATE INDEX ix_sec_sec2idkey_idkey ON sec_security2idkey(idkey_id);
-- sec_security_idkey is fully dependent of sec_security

-- Hibernate controlled tables
CREATE TABLE sec_currency (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_commodityfuturetype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_bondfuturetype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_cashrate (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_unit (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_exchange (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    description varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE sec_gics (
    id bigint NOT NULL,
    name varchar(8) NOT NULL UNIQUE,
    description varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE sec_equity (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    shortName varchar(255),
    exchange_id bigint NOT NULL,
    companyName varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    gicscode_id bigint,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equity2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equity2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
    CONSTRAINT sec_fk_equity2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange(id),
    CONSTRAINT sec_fk_equity2gics FOREIGN KEY (gicscode_id) REFERENCES sec_gics(id)
);
CREATE INDEX ix_sec_equity_security_id ON sec_equity(security_id);

CREATE TABLE sec_equityindexoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    exchange_id bigint,
    pointValue double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindexoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityindexoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_equityindexoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_equityoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    exchange_id bigint,
    pointValue double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_equityoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equityoption_security_id ON sec_equityoption(security_id);

CREATE TABLE sec_fxoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
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
    CONSTRAINT sec_fk_fxoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxoption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxoption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_nondeliverablefxoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
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
    CONSTRAINT sec_fk_nondeliverablefxoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);
CREATE INDEX ix_sec_nondeliverablefxoption_security_id ON sec_nondeliverablefxoption(security_id);

CREATE TABLE sec_swaption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    cash_settled boolean NOT NULL,
    is_long boolean NOT NULL,
    is_payer boolean NOT NULL,
    currency_id bigint NOT NULL,
    option_exercise_type VARCHAR(32),
    settlement_date TIMESTAMP,
    settlement_zone VARCHAR(50),
    notional double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_swaption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
    CONSTRAINT sec_fk_swaption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_irfutureoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    exchange_id bigint NOT NULL,
    margined boolean NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_irfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_irfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_irfutureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);

CREATE TABLE sec_fxbarrieroption (
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
    barrier_type varchar(32) NOT NULL,
    barrier_direction varchar(32) NOT NULL,
    barrier_level double precision NOT NULL,
    monitoring_type varchar(32) NOT NULL,
    sampling_frequency varchar(32),
    is_long boolean NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fxbarrieroption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxbarrieroption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxbarrieroption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_frequency (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_daycount (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_businessdayconvention (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE sec_issuertype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_market (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_yieldconvention (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_guaranteetype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_coupontype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
 );

CREATE TABLE sec_bond (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    bond_type varchar(32) NOT NULL,
    issuername varchar(255) NOT NULL,
    issuertype_id bigint NOT NULL,
    issuerdomicile varchar(255) NOT NULL,
    market_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    yieldconvention_id bigint NOT NULL,
    guaranteetype_id bigint,
    maturity_date timestamp without time zone NOT NULL,
    maturity_zone varchar(50) NOT NULL,
    maturity_accuracy smallint NOT NULL,
    coupontype_id bigint NOT NULL,
    couponrate double precision NOT NULL,
    couponfrequency_id bigint NOT NULL,
    daycountconvention_id bigint NOT NULL,
    businessdayconvention_id bigint,
    announcement_date timestamp without time zone,
    announcement_zone varchar(50),
    interestaccrual_date timestamp without time zone NOT NULL,
    interestaccrual_zone varchar(50) NOT NULL,
    settlement_date timestamp without time zone NOT NULL,
    settlement_zone varchar(50) NOT NULL,
    firstcoupon_date timestamp without time zone NOT NULL,
    firstcoupon_zone varchar(50) NOT NULL,
    issuanceprice double precision NOT NULL,
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

CREATE TABLE sec_future (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    future_type varchar(32) NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    tradingexchange_id bigint NOT NULL,
    settlementexchange_id bigint NOT NULL,
    currency1_id bigint,
    currency2_id bigint,
    currency3_id bigint,
    bondtype_id bigint,
    commoditytype_id bigint,
    unitname_id bigint,
    unitnumber double precision,
    unit_amount double precision,
    underlying_scheme varchar(255),
    underlying_identifier varchar(255), 
    bondFutureFirstDeliveryDate timestamp without time zone,
    bondFutureFirstDeliveryDate_zone varchar(50),
    bondFutureLastDeliveryDate timestamp without time zone,
    bondFutureLastDeliveryDate_zone varchar(50),
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_future2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_future2exchange1 FOREIGN KEY (tradingexchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_future2exchange2 FOREIGN KEY (settlementexchange_id) REFERENCES sec_exchange (id),
    CONSTRAINT sec_fk_future2currency1 FOREIGN KEY (currency1_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2currency2 FOREIGN KEY (currency2_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2currency3 FOREIGN KEY (currency3_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_future2bondfuturetype FOREIGN KEY (bondtype_id) REFERENCES sec_bondfuturetype (id),
    CONSTRAINT sec_fk_future2commodityfuturetype FOREIGN KEY (commoditytype_id) REFERENCES sec_commodityfuturetype (id),
    CONSTRAINT sec_fk_future2unit FOREIGN KEY (unitname_id) REFERENCES sec_unit (id)
);
CREATE INDEX ix_sec_future_security_id ON sec_future(security_id);

CREATE TABLE sec_futurebundle (
    id bigint NOT NULL,
    future_id bigint NOT NULL,
    startDate timestamp without time zone,
    endDate timestamp without time zone,
    conversionFactor double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_futurebundle2future FOREIGN KEY (future_id) REFERENCES sec_future (id)
);

CREATE TABLE sec_futurebundleidentifier (
    bundle_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_futurebundleidentifier2futurebundle FOREIGN KEY (bundle_id) REFERENCES sec_futurebundle (id)
);

CREATE TABLE sec_cash (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    region_scheme varchar(255) NOT NULL,
    region_identifier varchar(255) NOT NULL,
    maturity_date timestamp without time zone NOT NULL,
    maturity_zone varchar(50) NOT NULL,
    rate double precision NOT NULL,
    amount double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cash2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cash2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_fra (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    region_scheme varchar(255) NOT NULL,
    region_identifier varchar(255) NOT NULL,
    start_date timestamp without time zone NOT NULL,
    start_zone varchar(50) NOT NULL,
    end_date timestamp without time zone NOT NULL,
    end_zone varchar(50) NOT NULL,
    rate double precision NOT NULL,
    amount double precision NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    fixing_date timestamp without time zone NOT NULL,
    fixing_zone varchar(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fra2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fra2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_swap (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    swaptype varchar(32) NOT NULL,
    trade_date timestamp without time zone NOT NULL,
    trade_zone varchar(50) NOT NULL,
    effective_date timestamp without time zone NOT NULL,
    effective_zone varchar(50) NOT NULL,
    maturity_date timestamp without time zone NOT NULL,
    maturity_zone varchar(50) NOT NULL,
    forwardstart_date timestamp without time zone,
    forwardstart_zone varchar(50),
    counterparty varchar(255) NOT NULL,
    pay_legtype varchar(32) NOT NULL,
    pay_daycount_id bigint NOT NULL,
    pay_frequency_id bigint NOT NULL,
    pay_regionscheme varchar(255) NOT NULL,
    pay_regionid varchar(255) NOT NULL,
    pay_businessdayconvention_id bigint NOT NULL,
    pay_notionaltype varchar(32) NOT NULL,
    pay_notionalcurrency_id bigint,
    pay_notionalamount double precision,
    pay_notionalscheme varchar(255),
    pay_notionalid varchar(255),
    pay_rate double precision,
    pay_iseom boolean NOT NULL,
    pay_spread double precision,
    pay_rateidentifierscheme varchar(255),
    pay_rateidentifierid varchar(255),
    pay_floating_rate_type varchar(32),
    pay_settlement_days INTEGER,
    pay_gearing DOUBLE precision,
    pay_offset_fixing_id bigint,
    receive_legtype varchar(32) NOT NULL,
    receive_daycount_id bigint NOT NULL,
    receive_frequency_id bigint NOT NULL,
    receive_regionscheme varchar(255) NOT NULL,
    receive_regionid varchar(255) NOT NULL,
    receive_businessdayconvention_id bigint NOT NULL,
    receive_notionaltype varchar(32) NOT NULL,
    receive_notionalcurrency_id bigint,
    receive_notionalamount double precision,
    receive_notionalscheme varchar(255),
    receive_notionalid varchar(255),
    receive_rate double precision,
    receive_iseom boolean NOT NULL, 
    receive_spread double precision,
    receive_rateidentifierscheme varchar(255),
    receive_rateidentifierid varchar(255),
    receive_floating_rate_type varchar(32),
    receive_settlement_days INTEGER,
    receive_gearing DOUBLE precision,
    receive_offset_fixing_id bigint,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_swap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_payfreq2frequency FOREIGN KEY (pay_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_receivefreq2frequency FOREIGN KEY (receive_frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_payoffset2frequency FOREIGN KEY (pay_offset_fixing_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_recvoffset2frequency FOREIGN KEY (receive_offset_fixing_id) REFERENCES sec_frequency (id)
);
CREATE INDEX ix_sec_swap_security_id ON sec_swap(security_id);

CREATE TABLE sec_raw (
    security_id bigint NOT NULL,
    raw_data bytea NOT NULL,
    CONSTRAINT sec_fk_raw2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);

CREATE TABLE sec_fx (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    pay_currency_id bigint NOT NULL,
    receive_currency_id bigint NOT NULL,
    region_scheme varchar(255) NOT NULL,
    region_identifier varchar(255) NOT NULL,
    pay_amount double precision NOT NULL,
    receive_amount double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_fx2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_fxpay2currency FOREIGN KEY (pay_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_fxreceive2currency FOREIGN KEY (receive_currency_id) REFERENCES sec_currency (id)
);

CREATE TABLE sec_fxforward (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  region_scheme varchar(255) NOT NULL,
  region_identifier varchar(255) NOT NULL,
  underlying_scheme varchar(255) NOT NULL,
  underlying_identifier varchar(255) NOT NULL,
  forward_date timestamp without time zone NOT NULL,
  forward_zone varchar(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_fxforward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);
CREATE INDEX ix_sec_fxforward_security_id ON sec_fxforward(security_id);

CREATE TABLE sec_capfloor (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  currency_id bigint NOT NULL,
  daycountconvention_id bigint NOT NULL,
  frequency_id bigint NOT NULL,
  is_cap boolean NOT NULL,
  is_ibor boolean NOT NULL,
  is_payer boolean NOT NULL,
  maturity_date timestamp without time zone NOT NULL,
  maturity_zone varchar(50) NOT NULL,
  notional double precision NOT NULL,
  start_date timestamp without time zone NOT NULL,
  start_zone varchar(50) NOT NULL,
  strike double precision NOT NULL,
  underlying_scheme varchar(255) NOT NULL,
  underlying_identifier varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_capfloor2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_capfloor2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_capfloor2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_capfloor2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id)
);

CREATE TABLE  sec_capfloorcmsspread (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  currency_id bigint NOT NULL,
  daycountconvention_id bigint NOT NULL,
  frequency_id bigint NOT NULL,
  is_cap boolean NOT NULL,
  is_payer boolean NOT NULL,
  long_scheme varchar(255) NOT NULL,
  long_identifier varchar(255) NOT NULL,
  maturity_date timestamp without time zone NOT NULL,
  maturity_zone varchar(50) NOT NULL,
  notional double precision NOT NULL,
  short_scheme varchar(255) NOT NULL,
  short_identifier varchar(255) NOT NULL,
  start_date timestamp without time zone NOT NULL,
  start_zone varchar(50) NOT NULL,
  strike double precision NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_capfloorcmsspread2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_capfloorcmsspread2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_capfloorcmsspread2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_capfloorcmsspread2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id)
);

CREATE TABLE  sec_equity_variance_swap (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  annualization_factor double precision NOT NULL,
  currency_id bigint NOT NULL,
  first_observation_date timestamp without time zone NOT NULL,
  first_observation_zone varchar(50) NOT NULL,
  last_observation_date timestamp without time zone NOT NULL,
  last_observation_zone varchar(50) NOT NULL,
  notional double precision NOT NULL,
  observation_frequency_id bigint NOT NULL,
  parameterised_as_variance boolean NOT NULL,
  region_scheme varchar(255) NOT NULL,
  region_id varchar(255) NOT NULL,
  settlement_date timestamp without time zone NOT NULL,
  settlement_zone varchar(50) NOT NULL,
  spot_scheme varchar(255) NOT NULL,
  spot_id varchar(255) NOT NULL,
  strike double precision NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_equityvarianceswap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_equityvarianceswap2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  CONSTRAINT sec_fk_equityvarianceswap2frequency FOREIGN KEY (observation_frequency_id) REFERENCES sec_frequency (id)
);

CREATE SEQUENCE sec_security_attr_seq
    start with 1000 increment by 1 no cycle;

CREATE TABLE sec_security_attribute (
    id bigint not null,
    security_id bigint not null,
    security_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint sec_fk_securityattr2security foreign key (security_id) references sec_security (id),
    constraint sec_chk_uq_security_attribute unique (security_id, key, value)
);
-- security_oid is an optimization
-- sec_security_attribute is fully dependent of sec_security
CREATE INDEX ix_sec_security_attr_security_oid ON sec_security_attribute(security_oid);
CREATE INDEX ix_sec_security_attr_key ON sec_security_attribute(key);