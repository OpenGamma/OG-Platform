
-- create-db-security.sql: Security Master

create table sec_currency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_commodityfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_bondfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_cashrate (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_unit (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_identifier_association (
    id bigint not null,
    security_discriminator varchar(255),
    security_id bigint,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    validStartDate timestamp,
    validEndDate timestamp,
    primary key (id)
);

create table sec_exchange (
    id bigint not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_gics (
    id bigint not null,
    name varchar(8) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_equity (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    shortName varchar(255),
    first_version_descriminator varchar(255),
    first_version_id bigint,
    exchange_id bigint not null,
    companyName varchar(255) not null,
    currency_id bigint not null,
    gicscode_id bigint,
    primary key (id),
    constraint sec_fk_equity2equity foreign key (first_version_id) references sec_equity(id),
    constraint sec_fk_equity2currency foreign key (currency_id) references sec_currency(id),
    constraint sec_fk_equity2exchange foreign key (exchange_id) references sec_exchange(id),
    constraint sec_fk_equity2gics foreign key (gicscode_id) references sec_gics(id)
);

create table sec_option (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    option_security_type varchar(32) not null,
    option_exercise_type varchar(32) not null,
    option_payoff_style varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry_date timestamp not null,
    expiry_accuracy int2 not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    currency_id bigint not null,
    put_currency_id bigint,
    call_currency_id bigint,
    exchange_id bigint,
    counterparty varchar(255),
    power double precision,
    cap double precision,
    margined bool,
    pointValue double precision,
    payment double precision,
    lowerbound double precision,
    upperbound double precision,
    choose_date timestamp,
    choose_zone varchar(50),
    underlyingstrike double precision,
    underlyingexpiry_date timestamp,
    underlyingexpiry_accuracy smallint,
    reverse bool,
    primary key (id),
    constraint sec_fk_option2option foreign key (first_version_id) references sec_option (id),
    constraint sec_fk_option2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_option2putcurrency foreign key (put_currency_id) references sec_currency (id),
    constraint sec_fk_option2callcurrency foreign key (call_currency_id) references sec_currency (id),
    constraint sec_fk_option2exchange foreign key (exchange_id) references sec_exchange (id)
);

create table sec_frequency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_daycount (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_businessdayconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_issuertype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_market (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_yieldconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_guaranteetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_coupontype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_bond (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    bond_type varchar(32) not null,
    issuername varchar(255) not null,
    issuertype_id bigint not null,
    issuerdomicile varchar(255) not null,
    market_id bigint not null,
    currency_id bigint not null,
    yieldconvention_id bigint not null,
    guaranteetype_id bigint not null,
    maturity_date timestamp not null,
    maturity_accuracy int2 not null,
    coupontype_id bigint not null,
    couponrate double precision not null,
    couponfrequency_id bigint not null,
    daycountconvention_id bigint not null,
    businessdayconvention_id bigint not null,
    announcement_date timestamp not null,
    announcement_zone varchar(50) not null,
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
    constraint sec_fk_bond2bond foreign key (first_version_id) references sec_bond (id),
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

create table sec_future (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    future_type varchar(32) not null,
    expiry_date timestamp not null,
    expiry_accuracy int2 not null,
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
    primary key (id),
    constraint sec_fk_future2future foreign key (first_version_id) references sec_future (id),
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

create table sec_futurebundle (
    id bigint not null,
    future_id bigint not null,
    startDate timestamp,
    endDate timestamp,
    conversionFactor double precision not null,
    primary key (id),
    constraint sec_fk_futurebundle2future foreign key (future_id) references sec_future (id)
);

create table sec_futurebundleidentifier (
    bundle_id bigint not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (bundle_id, scheme, identifier),
    constraint sec_fk_futurebundleidentifier2futurebundle foreign key (bundle_id) references sec_futurebundle (id)
);

create table sec_cash (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    currency_id bigint not null,
    region_scheme varchar(255) not null,
    region_identifier varchar(255) not null,
    primary key (id),
    constraint sec_fk_cash2cash foreign key (first_version_id) references sec_cash (id),
    constraint sec_fk_cash2currency foreign key (currency_id) references sec_currency (id)
);

create table sec_fra (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    start_date timestamp not null,
    start_zone varchar(50) not null,
    end_date timestamp not null,
    end_zone varchar(50) not null,
    primary key (id),
    constraint sec_fk_fra2fra foreign key (first_version_id) references sec_fra (id)
);

create table sec_swap (
    id bigint not null,
    effectiveDateTime timestamp not null,
    deleted bool not null,
    lastModifiedDateTime timestamp not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
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
    constraint sec_fk_swap2swap foreign key (first_version_id) references sec_swap (id)
);
