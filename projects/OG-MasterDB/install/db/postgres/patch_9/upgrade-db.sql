
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