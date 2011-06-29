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