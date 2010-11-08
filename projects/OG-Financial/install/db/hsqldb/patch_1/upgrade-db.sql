alter table sec_cash
  add column currency_id bigint not null default 1,
  add column region_scheme varchar(255) not null default 'ISO 3166-1 2 Letter Code',
  add column region_identifier varchar(255) not null default 'GB',
  add constraint sec_fk_cash2currency foreign key (currency_id) references sec_currency (id);

alter table sec_cash
  alter column currency_id drop default,
  alter column region_scheme drop default,
  alter column region_identifier drop default;