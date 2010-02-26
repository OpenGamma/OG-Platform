
    create table currency (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table domain_specific_identifier_association (
        id bigint not null,
        security_discriminator varchar(255),
        security_id bigint,
        domain varchar(255) not null,
        identifier varchar(255) not null,
        validStartDate date,
        validEndDate date,
        primary key (id),
        unique (domain, identifier, validStartDate, validEndDate)
    );

    create table exchange (
        id bigint not null,
        name varchar(255) not null unique,
        description varchar(255),
        primary key (id)
    );

    create table gics (
      id bigint not null,
      name varchar(8) not null unique,
      description varchar(255),
      primary key (id)
    );

    create table equity (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        first_version_descriminator varchar(255),
        first_version_id bigint,
        exchange_id bigint not null,
        companyName varchar(255) not null,
        currency_id bigint not null,
        gicscode_id bigint not null,
        primary key (id),
      	constraint fk_equity2currency foreign key (currency_id) references currency(id),
      	constraint fk_equity2exchange foreign key (exchange_id) references exchange(id),
        constraint fk_equity2gics foreign key (gicscode_id) references gics(id)
    );
    
    create table equityoption (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        first_version_descriminator varchar(255),
        first_version_id bigint,
        equity_option_type varchar(32) not null,
        option_type varchar(32) not null,
        strike double precision not null,
        expiry date not null,
        underlyingIdentityKey varchar(255),
        currency_id bigint not null,
        exchange_id bigint not null,
        primary key (id),
        constraint fk_equityoption2currency foreign key (currency_id) references currency (id),
        constraint fk_equityoption2exchange foreign key (exchange_id) references exchange (id)
    );
    
    create table frequency (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table daycount (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table businessdayconvention (
        id bigint not null,
        name varchar(255) not null unique,
        primary key (id)
    );
    
    create table bond (
        id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        first_version_descriminator varchar(255),
        first_version_id bigint,
        bond_type varchar(32) not null,
        maturity date not null,
        coupon double precision not null,
        frequency_id bigint not null,
        country varchar(255) not null,
        credit_rating varchar(255) not null,
        currency_id bigint not null,
        issuer varchar(255) not null,
        daycount_id bigint not null,
        businessdayconvention_id bigint not null,
        primary key (id),
        constraint fk_bond2frequency foreign key (frequency_id) references frequency (id),
        constraint fk_bond2currency foreign key (currency_id) references currency (id),
        constraint fk_bond2daycount foreign key (daycount_id) references daycount (id),
        constraint fk_bond2businessdayconvention foreign key (businessdayconvention_id) references businessdayconvention (id)
    );

    create table hibernate_sequence (
         next_val bigint 
    );

    insert into hibernate_sequence values ( 1 );
