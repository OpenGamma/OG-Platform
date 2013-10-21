Guide to writing database SQL scripts
=====================================

This guide is generally written relative to HSQLDB.
Replace my_xxx with something appropriate.


Postgres
----------
Data types:

blob -> bytea

Sequences do not specify "AS BIGINT"

Can create indexes on upper case function

Populate column from sequence:

  id bigint NOT NULL DEFAULT nextval('hts_doc2idkey_seq'),


Converting a column from NULL to NOT NULL

  ALTER TABLE my_table ADD my_column my_data_type NULL;
  UPDATE my_table SET my_column = my_default_value;
  ALTER TABLE my_table ALTER COLUMN my_column SET NOT NULL;


Converting a column from NOT NULL to NULL

  ALTER TABLE my_table ALTER COLUMN my_column my_data_type DROP NOT NULL;



SQL Server
----------
Data types:

boolean -> bit
true -> 1
false -> 0
blob -> image
double -> double precision
timestamp without time zone -> DATETIME2(6)


No sequences


Updating the scheme version:

  UPDATE foo_schema_version SET version_value='my_value' WHERE version_key='schema_patch';


Converting a column from NULL to NOT NULL

  ALTER TABLE my_table ADD my_column my_data_type NULL;
  UPDATE my_table SET my_column = my_default_value;
  ALTER TABLE my_table ALTER COLUMN my_column my_data_type NOT NULL;


Converting a column from NOT NULL to NULL

  ALTER TABLE my_table ALTER COLUMN my_column my_data_type NULL;

