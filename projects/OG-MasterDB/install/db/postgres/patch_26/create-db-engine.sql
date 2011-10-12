
-- create-db-engine.sql: Config Master

create table eng_functioncosts (
    configuration varchar(255) not null,
    function varchar(255) not null,
    version_instant timestamp with time zone not null,
    invocation_cost decimal(31,8) not null,
    data_input_cost decimal(31,8) not null,
    data_output_cost decimal(31,8) not null,
    primary key (configuration, function, version_instant)
);
