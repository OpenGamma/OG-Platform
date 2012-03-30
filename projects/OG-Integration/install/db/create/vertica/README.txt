The Vertica DB is the same as the Postgres DB, except that
statements not supported by Vertica were removed:

- CHECK constraints
- CREATE INDEX

In addition:

- KSAFE was added. 
  Without this change DELETE {tablename} statements failed. 
  For an explanation why, see Vertica SQL Reference Manual - SQL Statements - CREATE TABLE.