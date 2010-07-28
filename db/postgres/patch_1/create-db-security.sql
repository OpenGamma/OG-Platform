
-- create-db-security.sql: Security Master

create table sec_currency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_commodityfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_bondfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_cashrate (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_unit (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_identifier_association (
    id int8 not null,
    security_discriminator varchar(255),
    security_id int8,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    validStartDate date,
    validEndDate date,
    primary key (id)
);

create table sec_exchange (
    id int8 not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_gics (
  id int8 not null,
  name varchar(8) not null unique,
  description varchar(255),
  primary key (id)
);

create table sec_equity (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    exchange_id int8 not null,
    companyName varchar(255) not null,
    currency_id int8 not null,
    gicscode_id int8,
    primary key (id),
    constraint sec_fk_equity2equity foreign key (first_version_id) references sec_equity(id),
  	constraint sec_fk_equity2currency foreign key (currency_id) references sec_currency(id),
  	constraint sec_fk_equity2exchange foreign key (exchange_id) references sec_exchange(id),
  	constraint sec_fk_equity2gics foreign key (gicscode_id) references sec_gics(id)
);

create table sec_option (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    option_security_type varchar(32) not null,
    option_exercise_type varchar(32) not null,
    option_payoff_style varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry date not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    power double precision,
    cap double precision,
    currency_id int8 not null,
    put_currency_id int8,
    call_currency_id int8,
    exchange_id int8,
    counterparty varchar(255),
    margined bool,
    pointValue double precision,
    primary key (id),
    constraint sec_fk_option2option foreign key (first_version_id) references sec_option (id),
    constraint sec_fk_option2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_option2putcurrency foreign key (put_currency_id) references sec_currency (id),
    constraint sec_fk_option2callcurrency foreign key (call_currency_id) references sec_currency (id),
    constraint sec_fk_option2exchange foreign key (exchange_id) references sec_exchange (id)
);

create table sec_frequency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_daycount (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_businessdayconvention (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_issuertype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_market (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_yieldconvention (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_guaranteetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_coupontype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_bond (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    bond_type varchar(32) not null,
    issuername varchar(255) not null,
    issuertype_id int8 not null,
    issuerdomicile varchar(255) not null,
    market_id int8 not null,
    currency_id int8 not null,
    yieldconvention_id int8 not null,
    guaranteetype_id int8 not null,
    maturity date not null,
    coupontype_id int8 not null,
    couponrate double precision not null,
    couponfrequency_id int8 not null,
    daycountconvention_id int8 not null,
    businessdayconvention_id int8 not null,
    announcementdate date not null,
    interestaccrualdate date not null,
    settlementdate date not null,
    firstcoupondate date not null,
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
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    future_type varchar(32) not null,
    expiry date not null,
    tradingexchange_id int8 not null,
    settlementexchange_id int8 not null,
    currency1_id int8,
    currency2_id int8,
    currency3_id int8,
    bondtype_id int8,
    commoditytype_id int8,
    cashratetype_id int8,
    unitname_id int8,
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
    id int8 not null,
    future_id int8 not null,
    startDate date,
    endDate date,
    conversionFactor double precision not null,
    primary key (id),
    constraint sec_fk_futurebundle2future foreign key (future_id) references sec_future (id)
);

create table sec_futurebundleidentifier (
    bundle_id int8 not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (bundle_id, scheme, identifier),
    constraint sec_fk_futurebundleidentifier2futurebundle foreign key (bundle_id) references sec_futurebundle (id)
);

create table sec_cash (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    primary key (id),
    constraint sec_fk_cash2cash foreign key (first_version_id) references sec_cash (id)
);

create table sec_fra (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    startDate date not null,
    endDate date not null,
    primary key (id),
    constraint sec_fk_fra2fra foreign key (first_version_id) references sec_fra (id)
);

create table sec_swap (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    swaptype varchar(32) not null,
    tradedate date not null,
    effectivedate date not null,
    maturitydate date not null,
    forwardstartdate date,
    counterparty varchar(255) not null,
    pay_legtype varchar(32) not null,
    pay_daycount_id int8 not null,
    pay_frequency_id int8 not null,
    pay_regionscheme varchar(255) not null,
    pay_regionid varchar(255) not null,
    pay_businessdayconvention_id int8 not null,
    pay_notionaltype varchar(32) not null,
    pay_notionalcurrency_id int8,
    pay_notionalamount double precision,
    pay_notionalscheme varchar(255),
    pay_notionalid varchar(255),
    pay_rate double precision,
    pay_spread double precision,
    pay_rateidentifierscheme varchar(255),
    pay_rateidentifierid varchar(255),
    receive_legtype varchar(32) not null,
    receive_daycount_id int8 not null,
    receive_frequency_id int8 not null,
    receive_regionscheme varchar(255) not null,
    receive_regionid varchar(255) not null,
    receive_businessdayconvention_id int8 not null,
    receive_notionaltype varchar(32) not null,
    receive_notionalcurrency_id int8,
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