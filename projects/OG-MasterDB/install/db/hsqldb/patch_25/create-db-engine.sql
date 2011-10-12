
-- create-db-engine.sql: Config Master

create table eng_functioncosts (
    configuration varchar(255) NOT NULL,
    function varchar(255) NOT NULL,
    version_instant timestamp without time zone NOT NULL,
    invocation_cost decimal(31,8) NOT NULL,
    data_input_cost decimal(31,8) NOT NULL,
    data_output_cost decimal(31,8) NOT NULL,
    PRIMARY KEY (configuration, function, version_instant)
);
