
CREATE TABLE sec_equityindexoption (
    id bigint not null,
    security_id bigint not null,
    option_exercise_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    currency_id bigint not null,
    exchange_id bigint,
    pointValue double precision,
    primary key (id),
    constraint sec_fk_equityindexoption2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_equityindexoption2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_equityindexoption2exchange foreign key (exchange_id) references sec_exchange (id)
);

CREATE TABLE sec_equityoption (
    id bigint not null,
    security_id bigint not null,
    option_exercise_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    currency_id bigint not null,
    exchange_id bigint,
    pointValue double precision,
    primary key (id),
    constraint sec_fk_equityoption2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_equityoption2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_equityoption2exchange foreign key (exchange_id) references sec_exchange (id)
);

CREATE TABLE sec_fxoption (
    id bigint not null,
    security_id bigint not null,
    put_amount double precision not null,
    call_amount double precision not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    put_currency_id bigint not null,
    call_currency_id bigint not null,
    settlement_date timestamp not null,
    settlement_zone varchar(50) not null,
    primary key (id),
    constraint sec_fk_fxoption2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_fxoption2putcurrency foreign key (put_currency_id) references sec_currency (id),
    constraint sec_fk_fxoption2callcurrency foreign key (call_currency_id) references sec_currency (id)
);

CREATE TABLE sec_swaption (
    id bigint not null,
    security_id bigint not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    cash_settled boolean not null,
    primary key (id),
    constraint sec_fk_swaption2sec foreign key (security_id) references sec_security (id)
);

CREATE TABLE sec_irfutureoption (
    id bigint not null,
    security_id bigint not null,
    option_exercise_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry_date timestamp not null,
    expiry_zone varchar(50) not null,
    expiry_accuracy smallint not null,
    underlying_scheme varchar(255) not null,
    underlying_identifier varchar(255) not null,
    currency_id bigint not null,
    exchange_id bigint not null,
    margined boolean not null,
    pointValue double precision not null,
    primary key (id),
    constraint sec_fk_irfutureoption2sec foreign key (security_id) references sec_security (id),
    constraint sec_fk_irfutureoption2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_irfutureoption2exchange foreign key (exchange_id) references sec_exchange (id)
);

begin;
insert into sec_equityoption (id, security_id, option_exercise_type, option_type, strike, expiry_date, expiry_zone, 
  expiry_accuracy, underlying_scheme, underlying_identifier, currency_id, exchange_id, pointValue) 

  select id, security_id, option_exercise_type, option_type, strike, expiry_date, expiry_zone,
    expiry_accuracy, underlying_scheme, underlying_identifier, currency_id, exchange_id, pointValue
    from sec_option where option_security_type = 'Equity'
commit;