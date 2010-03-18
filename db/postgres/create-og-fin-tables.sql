DROP TABLE IF EXISTS currency CASCADE;
DROP TABLE IF EXISTS commodityfuturetype CASCADE;
DROP TABLE IF EXISTS bondfuturetype CASCADE;
DROP TABLE IF EXISTS cashrate CASCADE;
DROP TABLE IF EXISTS unit CASCADE;
DROP TABLE IF EXISTS domain_specific_identifier_association CASCADE;
DROP TABLE IF EXISTS exchange CASCADE;
DROP TABLE IF EXISTS gics CASCADE;
DROP TABLE IF EXISTS equity CASCADE;
DROP TABLE IF EXISTS equityoption CASCADE;
DROP TABLE IF EXISTS frequency CASCADE;
DROP TABLE IF EXISTS daycount CASCADE;
DROP TABLE IF EXISTS businessdayconvention CASCADE;
DROP TABLE IF EXISTS bond CASCADE;
DROP TABLE IF EXISTS future CASCADE;
DROP TABLE IF EXISTS future_basket CASCADE;
	
DROP SEQUENCE IF EXISTS hibernate_sequence CASCADE;

create table currency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON currency FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE currency TO finupdater;
GRANT SELECT ON TABLE currency TO PUBLIC;

create table commodityfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON commodityfuturetype FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE commodityfuturetype TO finupdater;
GRANT SELECT ON TABLE commodityfuturetype TO PUBLIC;

create table bondfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

REVOKE ALL ON bondfuturetype FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE bondfuturetype TO finupdater;
GRANT SELECT ON TABLE bondfuturetype TO PUBLIC;

create table cashrate (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON cashrate FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE cashrate TO finupdater;
GRANT SELECT ON TABLE cashrate TO PUBLIC;

create table unit (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

REVOKE ALL ON unit FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE unit TO finupdater;
GRANT SELECT ON TABLE unit TO PUBLIC;    
    
create table domain_specific_identifier_association (
    id int8 not null,
    security_discriminator varchar(255),
    security_id int8,
    domain varchar(255) not null,
    identifier varchar(255) not null,
    validStartDate date,
    validEndDate date,
    primary key (id),
    unique (domain, identifier, validStartDate, validEndDate)
);

REVOKE ALL ON domain_specific_identifier_association FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE domain_specific_identifier_association TO finupdater;
GRANT SELECT ON TABLE domain_specific_identifier_association TO PUBLIC;

create table exchange (
    id int8 not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

REVOKE ALL ON exchange FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE exchange TO finupdater;
GRANT SELECT ON TABLE exchange TO PUBLIC;

create table gics (
  id int8 not null,
  name varchar(8) not null unique,
  description varchar(255),
  primary key (id)
);
    
REVOKE ALL ON gics FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE gics TO finupdater;
GRANT SELECT ON TABLE gics TO PUBLIC;    

create table equity (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    first_version_descriminator varchar(255),
    first_version_id int8,
    exchange_id int8 not null,
    companyName varchar(255) not null,
    currency_id int8 not null,
    gicscode_id int8 not null,
    primary key (id),
  	constraint fk_equity2currency foreign key (currency_id) references currency(id),
  	constraint fk_equity2exchange foreign key (exchange_id) references exchange(id),
  	constraint fk_equity2gics foreign key (gicscode_id) references gics(id)
);

REVOKE ALL ON equity FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE equity TO finupdater;
GRANT SELECT ON TABLE equity TO PUBLIC;

create table equityoption (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    first_version_descriminator varchar(255),
    first_version_id int8,
    equity_option_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry date not null,
    underlyingIdentityKey varchar(255),
    power double precision,
    currency_id int8 not null,
    exchange_id int8 not null,
    primary key (id),
    constraint fk_equityoption2currency foreign key (currency_id) references currency (id),
    constraint fk_equityoption2exchange foreign key (exchange_id) references exchange (id)
);

REVOKE ALL ON equityoption FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE equityoption TO finupdater;
GRANT SELECT ON TABLE equityoption TO PUBLIC;
    
create table frequency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON frequency FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE frequency TO finupdater;
GRANT SELECT ON TABLE frequency TO PUBLIC;
    
create table daycount (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON daycount FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE daycount TO finupdater;
GRANT SELECT ON TABLE daycount TO PUBLIC;
    
create table businessdayconvention (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);
    
REVOKE ALL ON businessdayconvention FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE businessdayconvention TO finupdater;
GRANT SELECT ON TABLE businessdayconvention TO PUBLIC;
    
create table bond (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    first_version_descriminator varchar(255),
    first_version_id int8,
    bond_type varchar(32) not null,
    maturity date not null,
    coupon double precision not null,
    frequency_id int8 not null,
    country varchar(255) not null,
    credit_rating varchar(255) not null,
    currency_id int8 not null,
    issuer varchar(255) not null,
    daycount_id int8 not null,
    businessdayconvention_id int8 not null,
    primary key (id),
    constraint fk_bond2frequency foreign key (frequency_id) references frequency (id),
    constraint fk_bond2currency foreign key (currency_id) references currency (id),
    constraint fk_bond2daycount foreign key (daycount_id) references daycount (id),
    constraint fk_bond2businessdayconvention foreign key (businessdayconvention_id) references businessdayconvention (id)
);
    
REVOKE ALL ON bond FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE bond TO finupdater;
GRANT SELECT ON TABLE bond TO PUBLIC;
    
create table future (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    first_version_descriminator varchar(255),
    first_version_id int8,
    future_type varchar(32) not null,
    expiry date not null,
    tradingexchange_id int8 not null,
    settlementexchange_id int8 not null,
    currency1_id int8,
    currency2_id int8,
    bondtype_id int8,
    commoditytype_id int8,
    cashratetype_id int8,
    unitname_id int8,
    unitnumber double precision, 
    primary key (id),
    constraint fk_future2exchange1 foreign key (tradingexchange_id) references exchange (id),
    constraint fk_future2exchange2 foreign key (settlementexchange_id) references exchange (id),
    constraint fk_future2currency1 foreign key (currency1_id) references currency (id),
    constraint fk_future2currency2 foreign key (currency2_id) references currency (id),
    constraint fk_future2bondfuturetype foreign key (bondtype_id) references bondfuturetype (id),
    constraint fk_future2commodityfuturetype foreign key (commoditytype_id) references commodityfuturetype (id),
    constraint fk_future2cashrate foreign key (cashratetype_id) references cashrate (id),
    constraint fk_future2unit foreign key (unitname_id) references unit (id)
);
    
REVOKE ALL ON future FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE future TO finupdater;
GRANT SELECT ON TABLE future TO PUBLIC;
    
create table future_basket (
    id int8 not null,
    future_id int8 not null,
    domain varchar(255) not null,
    identifier varchar(255) not null,
    primary key (id),
    constraint fk_future_basket2future foreign key (future_id) references future (id),
    unique (future_id, domain, identifier)
);
    
REVOKE ALL ON future_basket FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE future_basket TO finupdater;
GRANT SELECT ON TABLE future_basket TO PUBLIC;
    
create sequence hibernate_sequence start 1 increment 1;
GRANT SELECT, UPDATE ON SEQUENCE hibernate_sequence TO finupdater;