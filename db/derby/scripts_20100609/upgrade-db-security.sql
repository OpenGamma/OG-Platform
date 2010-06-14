
-- upgrade-db-security.sql: Security Master

alter table sec_option drop constraint sec_fk_option2currency1;
alter table sec_option drop constraint sec_fk_option2currency2;
alter table sec_option drop constraint sec_fk_option2currency3;

alter table sec_option drop column currency1_id;
alter table sec_option add currency_id bigint;

alter table sec_option drop column currency2_id;
alter table sec_option add put_currency_id bigint;

alter table sec_option drop column currency3_id;
alter table sec_option add call_currency_id bigint;

alter table sec_option add constraint sec_fk_option2currency foreign key (currency_id) references sec_currency (id);
alter table sec_option add constraint sec_fk_option2putcurrency foreign key (put_currency_id) references sec_currency (id);
alter table sec_option add constraint sec_fk_option2callcurrency foreign key (call_currency_id) references sec_currency (id);

alter table sec_option drop column underlyingIdentityKey;
alter table sec_option add column underlying_scheme varchar(255);
alter table sec_option add column underlying_identifier varchar(255);
    