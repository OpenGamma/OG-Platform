
    create table currency (
        id int8 not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table domain_specific_identifier_association (
        id int8 not null,
        security_discriminator varchar(255),
        security_id int8,
        domain varchar(255) not null,
        identifier varchar(255) not null,
        primary key (id),
        unique (domain, identifier)
    );

    create table exchange (
        id int8 not null,
        name varchar(255) not null unique,
        description varchar(255),
        primary key (id)
    );

    create table equity (
        id int8 not null,
        effectiveDateTime date not null,
        deleted bool not null,
        lastModifiedDateTime date not null,
        lastModifiedBy varchar(255),
        first_version_descriminator varchar(255),
        first_version_id int8,
        exchange_id int8 not null,
        companyName varchar(255) not null,
        currency_id int8 not null,
        primary key (id),
	constraint fk_equity2currency foreign key (currency_id) references currency(id),
	constraint fk_equity2exchange foreign key (exchange_id) references exchange(id)
    );


    create sequence hibernate_sequence start 1 increment 1;
