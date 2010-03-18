DROP DATABASE IF EXISTS og_financial;

DROP ROLE IF EXISTS finowner;

CREATE ROLE finowner LOGIN
NOSUPERUSER INHERIT CREATEDB CREATEROLE;
COMMENT ON ROLE finowner IS 'og_financial security storage owner';
ALTER ROLE finowner WITH PASSWORD 'ajT0xnJj';

DROP ROLE IF EXISTS finreader;

CREATE ROLE finreader LOGIN
NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
COMMENT ON ROLE finreader IS 'readonly account for og_financial';
ALTER ROLE finreader WITH PASSWORD 'ytN3qjEP';

DROP ROLE IF EXISTS finupdater;

CREATE ROLE finupdater LOGIN
NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
COMMENT ON ROLE finupdater IS 'update only account for og_financial';
ALTER ROLE finupdater WITH PASSWORD 'petNbxvn';

CREATE DATABASE og_financial WITH OWNER = finowner;