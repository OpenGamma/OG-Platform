
-- upgrade-db-security.sql: Security Master

alter table equity drop constraint fk_equity2currency;
alter table optionsec drop constraint fk_option2currency1;
alter table optionsec drop constraint fk_option2currency2;
alter table optionsec drop constraint fk_option2currency3;
alter table bond drop constraint fk_bond2currency;
alter table future drop constraint fk_future2currency1;
alter table future drop constraint fk_future2currency2;
alter table future drop constraint fk_future2currency3;
rename table currency to sec_currency;

alter table future drop constraint fk_future2commodityfuturetype;
rename table commodityfuturetype to sec_commodityfuturetype;

alter table future drop constraint fk_future2bondfuturetype;
rename table bondfuturetype to sec_bondfuturetype;

alter table future drop constraint fk_future2cashrate;
rename table cashrate to sec_cashrate;

alter table future drop constraint fk_future2unit;
rename table unit to sec_unit;

rename table domain_specific_identifier_association to sec_identifier_association;
rename column sec_identifier_association.domain to scheme;

alter table future drop constraint fk_future2exchange1;
alter table future drop constraint fk_future2exchange2;
alter table equity drop constraint fk_equity2exchange;
alter table optionsec drop constraint fk_option2exchange;
rename table exchange to sec_exchange;

alter table equity drop constraint fk_equity2gics;
rename table gics to sec_gics;

rename table equity to sec_equity;
alter table sec_equity add constraint sec_fk_equity2currency foreign key (currency_id) references sec_currency(id);
alter table sec_equity add constraint sec_fk_equity2exchange foreign key (exchange_id) references sec_exchange(id);
alter table sec_equity add constraint sec_fk_equity2gics foreign key (gicscode_id) references sec_gics(id);

rename table optionsec to sec_option;
alter table sec_option add constraint sec_fk_option2currency1 foreign key (currency1_id) references sec_currency (id);
alter table sec_option add constraint sec_fk_option2currency2 foreign key (currency2_id) references sec_currency (id);
alter table sec_option add constraint sec_fk_option2currency3 foreign key (currency3_id) references sec_currency (id);
alter table sec_option add constraint sec_fk_option2exchange foreign key (exchange_id) references sec_exchange (id);

alter table bond drop constraint fk_bond2frequency;
rename table frequency to sec_frequency;

alter table bond drop constraint fk_bond2daycount;
rename table daycount to sec_daycount;

alter table bond drop constraint fk_bond2businessdayconvention;
rename table businessdayconvention to sec_businessdayconvention;

alter table bond drop constraint fk_bond2issuertype;
rename table issuertype to sec_issuertype;

alter table bond drop constraint fk_bond2market;
rename table market to sec_market;

alter table bond drop constraint fk_bond2yieldconvention;
rename table yieldconvention to sec_yieldconvention;

alter table bond drop constraint fk_bond2guaranteetype;
rename table guaranteetype to sec_guaranteetype;

alter table bond drop constraint fk_bond2coupontype;
rename table coupontype to sec_coupontype;

rename table bond to sec_bond;
alter table sec_bond add constraint sec_fk_bond2currency foreign key (currency_id) references sec_currency (id);
alter table sec_bond add constraint sec_fk_bond2issuertype foreign key (issuertype_id) references sec_issuertype (id);
alter table sec_bond add constraint sec_fk_bond2market foreign key (market_id) references sec_market (id);
alter table sec_bond add constraint sec_fk_bond2yieldconvention foreign key (yieldconvention_id) references sec_yieldconvention (id);
alter table sec_bond add constraint sec_fk_bond2guaranteetype foreign key (guaranteetype_id) references sec_guaranteetype (id);
alter table sec_bond add constraint sec_fk_bond2coupontype foreign key (coupontype_id) references sec_coupontype (id);
alter table sec_bond add constraint sec_fk_bond2frequency foreign key (couponfrequency_id) references sec_frequency (id);
alter table sec_bond add constraint sec_fk_bond2daycount foreign key (daycountconvention_id) references sec_daycount (id);
alter table sec_bond add constraint sec_fk_bond2businessdayconvention foreign key (businessdayconvention_id) references sec_businessdayconvention (id);

alter table future_basket drop constraint fk_future_basket2future;
rename table future to sec_future;
alter table sec_future add underlying_scheme varchar(255);
alter table sec_future add underlying_identifier varchar(255); 
alter table sec_future add constraint sec_fk_future2currency1 foreign key (currency1_id) references sec_currency (id);
alter table sec_future add constraint sec_fk_future2currency2 foreign key (currency2_id) references sec_currency (id);
alter table sec_future add constraint sec_fk_future2currency3 foreign key (currency3_id) references sec_currency (id);
alter table sec_future add constraint sec_fk_future2commodityfuturetype foreign key (commoditytype_id) references sec_commodityfuturetype (id);
alter table sec_future add constraint sec_fk_future2exchange1 foreign key (tradingexchange_id) references sec_exchange (id);
alter table sec_future add constraint sec_fk_future2exchange2 foreign key (settlementexchange_id) references sec_exchange (id);
alter table sec_future add constraint sec_fk_future2bondfuturetype foreign key (bondtype_id) references sec_bondfuturetype (id);
alter table sec_future add constraint sec_fk_future2cashrate foreign key (cashratetype_id) references sec_cashrate (id);
alter table sec_future add constraint sec_fk_future2unit foreign key (unitname_id) references sec_unit (id);

create table sec_futurebundleidentifier (
    bundle_id bigint not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (bundle_id, scheme, identifier)
);
insert into sec_futurebundleidentifier(bundle_id,scheme,identifier) select id,domain,identifier from future_basket;

rename table future_basket to sec_futurebundle;
alter table sec_futurebundle add constraint sec_fk_futurebundle2future foreign key (future_id) references sec_future (id);
alter table sec_futurebundle drop column domain;
alter table sec_futurebundle drop column identifier;
alter table sec_futurebundle add startDate date;
alter table sec_futurebundle add endDate date;
alter table sec_futurebundle add conversionFactor double not null default 1;
alter table sec_futurebundle alter conversionFactor drop default;

alter table sec_futurebundleidentifier add constraint sec_fk_futurebundleidentifier2futurebundle foreign key (bundle_id) references sec_futurebundle (id);