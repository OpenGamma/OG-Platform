
-- create-db-security.sql: Security Master

-- design has one document
--  security and associated identity key
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE sec_security_seq as bigint
    start with 1000 increment by 1 no cycle;
CREATE SEQUENCE sec_idkey_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE sec_security (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    sec_type varchar(255) not null,
    primary key (id),
    constraint sec_fk_sec2sec foreign key (oid) references sec_security (id),
    constraint sec_chk_sec_ver_order check (ver_from_instant <= ver_to_instant),
    constraint sec_chk_sec_corr_order check (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_sec_security_oid ON sec_security(oid);
CREATE INDEX ix_sec_security_ver_from_instant ON sec_security(ver_from_instant);
CREATE INDEX ix_sec_security_ver_to_instant ON sec_security(ver_to_instant);
CREATE INDEX ix_sec_security_corr_from_instant ON sec_security(corr_from_instant);
CREATE INDEX ix_sec_security_corr_to_instant ON sec_security(corr_to_instant);
CREATE INDEX ix_sec_security_name ON sec_security(name);
-- CREATE INDEX ix_sec_security_nameu ON sec_security(upper(name));
CREATE INDEX ix_sec_security_sec_type ON sec_security(sec_type);

CREATE TABLE sec_idkey (
    id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    primary key (id),
    constraint sec_chk_idkey unique (key_scheme, key_value)
);

CREATE TABLE sec_security2idkey (
    security_id bigint not null,
    idkey_id bigint not null,
    primary key (security_id, idkey_id),
    constraint sec_fk_secidkey2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_secidkey2idkey foreign key (idkey_id) references sec_idkey (id)
);
-- sec_security_idkey is fully dependent of sec_security

-- Hibernate controlled tables
CREATE TABLE sec_currency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_commodityfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_bondfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_cashrate (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_unit (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_exchange (
    id bigint not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

CREATE TABLE sec_gics (
    id bigint not null,
    name varchar(8) not null unique,
    description varchar(255),
    primary key (id)
);

CREATE TABLE sec_equity (
    id bigint not null,
    security_id bigint not null,
    shortName varchar(255),
    exchange_id bigint not null,
    companyName varchar(255) not null,
    currency_id bigint not null,
    gicscode_id bigint,
    primary key (id),
    constraint sec_fk_equity2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_equity2currency foreign key (currency_id) references sec_currency(id),
    constraint sec_fk_equity2exchange foreign key (exchange_id) references sec_exchange(id),
    constraint sec_fk_equity2gics foreign key (gicscode_id) references sec_gics(id)
);

CREATE TABLE sec_option (
    id bigint not null,
    security_id bigint not null,
    option_security_type varchar(32) not null,
    option_exercise_type varchar(32) not null,
    option_payoff_style varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    currency_id bigint not null,
    put_currency_id bigint,
    call_currency_id bigint,
    exchange_id bigint,
    counterparty varchar(255),
    power double precision,
    cap double precision,
    margined boolean,
    pointValue double precision,
    payment double precision,
    lowerbound double precision,
    upperbound double precision,
    choose_date timestamp,
    choose_zone varchar(50),
    underlyingstrike double precision,
    underlyingexpiry_date timestamp,
    underlyingexpiry_zone varchar(50),
    underlyingexpiry_accuracy smallint,
    reverse boolean,
    primary key (id),
    constraint sec_fk_option2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_option2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_option2putcurrency foreign key (put_currency_id) references sec_currency (id),
    constraint sec_fk_option2callcurrency foreign key (call_currency_id) references sec_currency (id),
    constraint sec_fk_option2exchange foreign key (exchange_id) references sec_exchange (id)
);

CREATE TABLE sec_frequency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_daycount (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_businessdayconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE sec_issuertype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

CREATE TABLE sec_market (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

CREATE TABLE sec_yieldconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

CREATE TABLE sec_guaranteetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

CREATE TABLE sec_coupontype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

CREATE TABLE sec_bond (
    id bigint not null,
    security_id bigint not null,
    bond_type varchar(32) not null,
    issuername varchar(255) not null,
    issuertype_id bigint not null,
    issuerdomicile varchar(255) not null,
    market_id bigint not null,
    currency_id bigint not null,
    yieldconvention_id bigint not null,
    guaranteetype_id bigint,
    maturity_date timestamp not null,
    maturity_zone varchar(50) not null,
    maturity_accuracy smallint not null,
    coupontype_id bigint not null,
    couponrate double precision not null,
    couponfrequency_id bigint not null,
    daycountconvention_id bigint not null,
    businessdayconvention_id bigint,
    announcement_date timestamp,
    announcement_zone varchar(50),
    interestaccrual_date timestamp not null,
    interestaccrual_zone varchar(50) not null,
    settlement_date timestamp not null,
    settlement_zone varchar(50) not null,
    firstcoupon_date timestamp not null,
    firstcoupon_zone varchar(50) not null,
    issuanceprice double precision not null,
    totalamountissued double precision not null,
    minimumamount double precision not null,
    minimumincrement double precision not null,
    paramount double precision not null,
    redemptionvalue double precision not null,
    primary key (id),
    constraint sec_fk_bond2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_bond2issuertype foreign key (issuertype_id) references sec_issuertype (id),
    constraint sec_fk_bond2market foreign key (market_id) references sec_market (id),
    constraint sec_fk_bond2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_bond2yieldconvention foreign key (yieldconvention_id) references sec_yieldconvention (id),
    constraint sec_fk_bond2guaranteetype foreign key (guaranteetype_id) references sec_guaranteetype (id),
    constraint sec_fk_bond2coupontype foreign key (coupontype_id) references sec_coupontype (id),
    constraint sec_fk_bond2frequency foreign key (couponfrequency_id) references sec_frequency (id),
    constraint sec_fk_bond2daycount foreign key (daycountconvention_id) references sec_daycount (id),
    constraint sec_fk_bond2businessdayconvention foreign key (businessdayconvention_id) references sec_businessdayconvention (id)
);

CREATE TABLE sec_future (
    id bigint not null,
    security_id bigint not null,
    future_type varchar(32) not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    tradingexchange_id bigint not null,
    settlementexchange_id bigint not null,
    currency1_id bigint,
    currency2_id bigint,
    currency3_id bigint,
    bondtype_id bigint,
    commoditytype_id bigint,
    cashratetype_id bigint,
    unitname_id bigint,
    unitnumber double precision,
    underlying_scheme varchar(255),
    underlying_identifier varchar(255), 
    bondFutureFirstDeliveryDate timestamp,
    bondFutureFirstDeliveryDate_zone varchar(50),
    bondFutureLastDeliveryDate timestamp,
    bondFutureLastDeliveryDate_zone varchar(50),
    primary key (id),
    constraint sec_fk_future2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_future2exchange1 foreign key (tradingexchange_id) references sec_exchange (id),
    constraint sec_fk_future2exchange2 foreign key (settlementexchange_id) references sec_exchange (id),
    constraint sec_fk_future2currency1 foreign key (currency1_id) references sec_currency (id),
    constraint sec_fk_future2currency2 foreign key (currency2_id) references sec_currency (id),
    constraint sec_fk_future2currency3 foreign key (currency3_id) references sec_currency (id),
    constraint sec_fk_future2bondfuturetype foreign key (bondtype_id) references sec_bondfuturetype (id),
    constraint sec_fk_future2commodityfuturetype foreign key (commoditytype_id) references sec_commodityfuturetype (id),
    constraint sec_fk_future2cashrate foreign key (cashratetype_id) references sec_cashrate (id),
    constraint sec_fk_future2unit foreign key (unitname_id) references sec_unit (id)
);

CREATE TABLE sec_futurebundle (
    id bigint not null,
    future_id bigint not null,
    startDate timestamp,
    endDate timestamp,
    conversionFactor double precision not null,
    primary key (id),
    constraint sec_fk_futurebundle2future foreign key (future_id) references sec_future (id)
);

CREATE TABLE sec_futurebundleidentifier (
    bundle_id bigint not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (bundle_id, scheme, identifier),
    constraint sec_fk_futurebundleidentifier2futurebundle foreign key (bundle_id) references sec_futurebundle (id)
);

CREATE TABLE sec_cash (
    id bigint not null,
    security_id bigint not null,
    currency_id bigint not null,
    region_scheme varchar(255) not null,
    region_identifier varchar(255) not null,
    maturity_date timestamp not null,
    maturity_zone varchar(50) not null,
    rate double precision not null,
    amount double precision not null,
    primary key (id),
    constraint sec_fk_cash2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_cash2currency foreign key (currency_id) references sec_currency (id)
);

CREATE TABLE sec_fra (
    id bigint not null,
    security_id bigint not null,
    currency_id bigint not null,
    region_scheme varchar(255) not null,
    region_identifier varchar(255) not null,
    start_date timestamp not null,
    start_zone varchar(50) not null,
    end_date timestamp not null,
    end_zone varchar(50) not null,
    rate double precision not null,
    amount double precision not null,
    primary key (id),
    constraint sec_fk_fra2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_fra2currency foreign key (currency_id) references sec_currency (id)
);

CREATE TABLE sec_swap (
    id bigint not null,
    security_id bigint not null,
    swaptype varchar(32) not null,
    trade_date timestamp not null,
    trade_zone varchar(50) not null,
    effective_date timestamp not null,
    effective_zone varchar(50) not null,
    maturity_date timestamp not null,
    maturity_zone varchar(50) not null,
    forwardstart_date timestamp,
    forwardstart_zone varchar(50),
    counterparty varchar(255) not null,
    pay_legtype varchar(32) not null,
    pay_daycount_id bigint not null,
    pay_frequency_id bigint not null,
    pay_regionscheme varchar(255) not null,
    pay_regionid varchar(255) not null,
    pay_businessdayconvention_id bigint not null,
    pay_notionaltype varchar(32) not null,
    pay_notionalcurrency_id bigint,
    pay_notionalamount double precision,
    pay_notionalscheme varchar(255),
    pay_notionalid varchar(255),
    pay_rate double precision,
    pay_spread double precision,
    pay_rateidentifierscheme varchar(255),
    pay_rateidentifierid varchar(255),
    receive_legtype varchar(32) not null,
    receive_daycount_id bigint not null,
    receive_frequency_id bigint not null,
    receive_regionscheme varchar(255) not null,
    receive_regionid varchar(255) not null,
    receive_businessdayconvention_id bigint not null,
    receive_notionaltype varchar(32) not null,
    receive_notionalcurrency_id bigint,
    receive_notionalamount double precision,
    receive_notionalscheme varchar(255),
    receive_notionalid varchar(255),
    receive_rate double precision,
    receive_spread double precision,
    receive_rateidentifierscheme varchar(255),
    receive_rateidentifierid varchar(255),
    primary key (id),
    constraint sec_fk_swap2sec foreign key (security_id) references sec_security (id)
);
