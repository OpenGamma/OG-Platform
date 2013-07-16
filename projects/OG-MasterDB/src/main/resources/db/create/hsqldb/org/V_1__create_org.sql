
-- create-db-organisation.sql

-- Organisation Master design has one organisation document
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE org_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO org_schema_version (version_key, version_value) VALUES ('schema_patch', '1');
  
CREATE SEQUENCE org_organisation_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby/HSQL, not accepted by Postgresql

CREATE TABLE org_organisation (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    provider_scheme varchar(255),
    provider_value varchar(255),

    obligor_short_name                           varchar(255) NOT NULL,
    obligor_red_code                             varchar(255) NOT NULL,
    obligor_ticker                               varchar(255) NOT NULL,
    obligor_country                              varchar(255) NOT NULL,
    obligor_region                               varchar(255) NOT NULL,
    obligor_sector                               varchar(255) NOT NULL,
    obligor_composite_rating                     varchar(255) NOT NULL,
    obligor_implied_rating                       varchar(255) NOT NULL,
    obligor_fitch_credit_rating                  varchar(255) NOT NULL,
    obligor_moodys_credit_rating                 varchar(255) NOT NULL,
    obligor_standard_and_poors_credit_rating     varchar(255) NOT NULL,
    obligor_has_defaulted                        smallint NOT NULL,
    CONSTRAINT org_chk_organisation_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT org_chk_organisation_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_org_organisation_oid ON org_organisation(oid);
CREATE INDEX ix_org_organisation_ver_from_instant ON org_organisation(ver_from_instant);
CREATE INDEX ix_org_organisation_ver_to_instant ON org_organisation(ver_to_instant);
CREATE INDEX ix_org_organisation_corr_from_instant ON org_organisation(corr_from_instant);
CREATE INDEX ix_org_organisation_corr_to_instant ON org_organisation(corr_to_instant);

CREATE INDEX ix_org_organisation_short_name ON org_organisation(obligor_short_name);
CREATE INDEX ix_org_organisation_red_code ON org_organisation(obligor_red_code);
CREATE INDEX ix_org_organisation_ticker ON org_organisation(obligor_ticker);

CREATE INDEX ix_org_organisation_composite_rating ON org_organisation(obligor_composite_rating);
CREATE INDEX ix_org_organisation_fitch_credit_rating ON org_organisation(obligor_fitch_credit_rating);
CREATE INDEX ix_org_organisation_moodys_credit_rating ON org_organisation(obligor_moodys_credit_rating);
CREATE INDEX ix_org_organisation_standard_and_poors_credit_rating ON org_organisation(obligor_standard_and_poors_credit_rating);
CREATE INDEX ix_org_organisation_implied_rating ON org_organisation(obligor_implied_rating);

CREATE INDEX ix_org_organisation_country ON org_organisation(obligor_country);
CREATE INDEX ix_org_organisation_region ON org_organisation(obligor_region);
CREATE INDEX ix_org_organisation_sector ON org_organisation(obligor_sector);
CREATE INDEX ix_org_organisation_has_defaulted ON org_organisation(obligor_has_defaulted);
