alter table sec_cash
  add column maturity_date not null default '2008-08-08 20:08:08'
  add column maturity_zone not null default 'UTC'

alter table sec_cash
  alter column maturity_date drop default,
  alter column maturity_zone drop default