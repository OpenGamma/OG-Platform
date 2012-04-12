
-- create-db-engine.sql: Config Master
CREATE TABLE eng_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO eng_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

create table eng_functioncosts (
    configuration VARCHAR(255) NOT NULL,
    function_name VARCHAR(255) NOT NULL,
    version_instant DATETIME2(6) NOT NULL,
    invocation_cost DECIMAL(31,8) NOT NULL,
    data_input_cost DECIMAL(31,8) NOT NULL,
    data_output_cost DECIMAL(31,8) NOT NULL,
    PRIMARY KEY (configuration, function_name, version_instant)
);
