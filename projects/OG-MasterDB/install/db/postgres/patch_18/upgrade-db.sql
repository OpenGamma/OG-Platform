CREATE TABLE sec_fx (
    id bigint not null,
    security_id bigint not null,
    pay_currency_id bigint not null,
    receive_currency_id bigint not null,
    region_scheme varchar(255) not null,
    region_identifier varchar(255) not null,
    pay_amount double precision not null,
    receive_amount double precision not null,
    primary key (id),
    constraint sec_fk_fx2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_fxpay2currency foreign key (pay_currency_id) references sec_currency (id),
    constraint sec_fk_fxreceive2currency foreign key (receive_currency_id) references sec_currency (id)
);

CREATE TABLE sec_fxforward (
  id bigint not null,
  security_id bigint not null,
  region_scheme varchar(255) not null,
  region_identifier varchar(255) not null,
  underlying_scheme varchar(255) not null,
  underlying_identifier varchar(255) not null,
  forward_date timestamp not null,
  forward_zone varchar(50) not null,
  primary key (id),
  constraint sec_fk_fxforward2sec foreign key (security_id) references sec_security (id)
);

CREATE TABLE sec_capfloor (
  id bigint not null,
  security_id bigint not null,
  currency_id bigint not null,
  daycountconvention_id bigint not null,
  frequency_id bigint not null,
  is_cap boolean not null,
  is_ibor boolean not null,
  is_payer boolean not null,
  maturity_date timestamp not null,
  maturity_zone varchar(50) not null,
  notional double precision not null,
  start_date timestamp not null,
  start_zone varchar(50) not null,
  strike double precision not null,
  underlying_scheme varchar(255) not null,
  underlying_identifier varchar(255) not null,
  
  primary key (id),
  constraint sec_fk_capfloor2sec foreign key (security_id) references sec_security (id),
  constraint sec_fk_capfloor2currency foreign key (currency_id) references sec_currency(id),
  constraint sec_fk_capfloor2daycount foreign key (daycountconvention_id) references sec_daycount (id),
  constraint sec_fk_capfloor2frequency foreign key (frequency_id) references sec_frequency (id)
);

CREATE TABLE  sec_capfloorcmsspread (
  id bigint not null,
  security_id bigint not null,
  currency_id bigint not null,
  daycountconvention_id bigint not null,
  frequency_id bigint not null,
  is_cap boolean not null,
  is_payer boolean not null,
  long_scheme varchar(255) not null,
  long_identifier varchar(255) not null,
  maturity_date timestamp not null,
  maturity_zone varchar(50) not null,
  notional double precision not null,
  short_scheme varchar(255) not null,
  short_identifier varchar(255) not null,
  start_date timestamp not null,
  start_zone varchar(50) not null,
  strike double precision not null,
  
  primary key (id),
  constraint sec_fk_capfloorcmsspread2sec foreign key (security_id) references sec_security (id),
  constraint sec_fk_capfloorcmsspread2currency foreign key (currency_id) references sec_currency(id),
  constraint sec_fk_capfloorcmsspread2daycount foreign key (daycountconvention_id) references sec_daycount (id),
  constraint sec_fk_capfloorcmsspread2frequency foreign key (frequency_id) references sec_frequency (id)
);