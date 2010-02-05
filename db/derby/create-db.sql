
    create table currencies (
        currency_id bigint not null,
        name varchar(255) not null unique,
        primary key (currency_id)
    );

    create table domain_specific_identifier_associations (
        domain_specific_identifier_id bigint not null,
        security_discriminator varchar(255),
        security_id bigint,
        domain varchar(255) not null,
        identifier varchar(255) not null,
        primary key (domain_specific_identifier_id),
        unique (domain, identifier)
    );

    create table exchanges (
        exchange_id bigint not null,
        name varchar(255) not null unique,
        description varchar(255),
        primary key (exchange_id)
    );

    create table equities (
        equities_id bigint not null,
        effectiveDateTime date not null,
        deleted smallint not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        first_version_descriminator varchar(255),
        first_version_id bigint,
        exchange_id bigint not null,
        companyName varchar(255) not null,
        currency_id bigint not null,
        primary key (equities_id),
	constraint fk_equities2currencies foreign key (currency_id) references currencies(currency_id),
	constraint fk_equities2exchanges foreign key (exchange_id) references exchanges(exchange_id)
    );

    create table hibernate_sequence (
         next_val bigint 
    );

    insert into hibernate_sequence values ( 1 );
