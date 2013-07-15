/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.masterdb.orgs.DbOrganizationMaster;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbOrganizationMasterWorker via DbOrganizationMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbOrganizationMasterWorkerTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbOrganizationMasterWorkerTest.class);

  protected DbOrganizationMaster _orgMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalOrganizations;

  final static String PROVIDER_SCHEME = "Markit";

  public AbstractDbOrganizationMasterWorkerTest(String databaseType, String databaseVersion, boolean readOnly) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    init();
  }

  @Override
  protected void doTearDown() {
    _orgMaster = null;
  }

  @Override
  protected void doTearDownClass() {
    _orgMaster = null;
  }

  //-------------------------------------------------------------------------
  private void init() {
    _orgMaster = new DbOrganizationMaster(getDbConnector());

//    id bigint NOT NULL,
//    oid bigint NOT NULL,
//    ver_from_instant timestamp without time zone NOT NULL,
//    ver_to_instant timestamp without time zone NOT NULL,
//    corr_from_instant timestamp without time zone NOT NULL,
//    corr_to_instant timestamp without time zone NOT NULL,
//    provider_scheme varchar(255),
//    provider_value varchar(255),
//
//    obligor_short_name                           varchar(255) NOT NULL,
//    obligor_red_code                             varchar(255) NOT NULL,
//    obligor_ticker                               varchar(255) NOT NULL,
//    obligor_country                              varchar(255) NOT NULL,
//    obligor_region                               varchar(255) NOT NULL,
//    obligor_sector                               varchar(255) NOT NULL,
//    obligor_composite_rating                     varchar(255) NOT NULL,
//    obligor_implied_rating                       varchar(255) NOT NULL,
//    obligor_fitch_credit_rating                  varchar(255) NOT NULL,
//    obligor_moodys_credit_rating                 varchar(255) NOT NULL,
//    obligor_standard_and_poors_credit_rating     varchar(255) NOT NULL,
//    obligor_has_defaulted                        smallint NOT NULL,

    Instant now = Instant.now();
    _orgMaster.setClock(Clock.fixed(now, ZoneId.of("UTC")));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);

    final JdbcOperations template = _orgMaster.getDbConnector().getJdbcOperations();

//    id bigint NOT NULL,
//    oid bigint NOT NULL,
//    ver_from_instant timestamp without time zone NOT NULL,
//        ver_to_instant timestamp without time zone NOT NULL,
//    corr_from_instant timestamp without time zone NOT NULL,
//        corr_to_instant timestamp without time zone NOT NULL,
//    provider_scheme varchar(255),
//        provider_value varchar(255),
//
//        obligor_short_name                           varchar(255) NOT NULL,
//    obligor_red_code                             varchar(255) NOT NULL,
//    obligor_ticker                               varchar(255) NOT NULL,
//    obligor_country                              varchar(255) NOT NULL,
//    obligor_region                               varchar(255) NOT NULL,
//    obligor_sector                               varchar(255) NOT NULL,
//    obligor_composite_rating                     varchar(255) NOT NULL,
//    obligor_implied_rating                       varchar(255) NOT NULL,
//    obligor_fitch_credit_rating                  varchar(255) NOT NULL,
//    obligor_moodys_credit_rating                 varchar(255) NOT NULL,
//    obligor_standard_and_poors_credit_rating     varchar(255) NOT NULL,
//    obligor_has_defaulted                        smallint NOT NULL,
//
    template.update("INSERT INTO org_organisation VALUES (?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)",
                    101,
                    101,
                    toSqlTimestamp(_version1Instant),
                    MAX_SQL_TIMESTAMP,
                    toSqlTimestamp(_version1Instant),
                    MAX_SQL_TIMESTAMP,
                    PROVIDER_SCHEME,
                    "1",
                    "TestOrganization101",
                    "RED_code_101",
                    "ticker_101",
                    "CountryA",
        Region.AFRICA.name(),
        Sector.BASICMATERIALS.name(),
        CreditRating.A.name(),
        CreditRating.A.name(),
        CreditRatingFitch.A.name(),
        CreditRatingMoodys.A.name(),
        CreditRatingStandardAndPoors.A.name(),
        0);
    template.update("INSERT INTO org_organisation VALUES (?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)",
                    102,
                    102,
                    toSqlTimestamp(_version1Instant),
                    MAX_SQL_TIMESTAMP,
                    toSqlTimestamp(_version1Instant),
                    MAX_SQL_TIMESTAMP,
                    PROVIDER_SCHEME,
                    "2",
                    "TestOrganization102",
                    "RED_code_102",
                    "ticker_102",
                    "CountryB",
        Region.AFRICA.name(),
        Sector.BASICMATERIALS.name(),
        CreditRating.A.name(),
        CreditRating.A.name(),
        CreditRatingFitch.A.name(),
        CreditRatingMoodys.A.name(),
        CreditRatingStandardAndPoors.A.name(),
        0);

    template.update("INSERT INTO org_organisation VALUES (?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)",
                    201,
                    201,
                    toSqlTimestamp(_version1Instant),
        toSqlTimestamp(_version2Instant),
        toSqlTimestamp(_version1Instant),
                    MAX_SQL_TIMESTAMP,
                    PROVIDER_SCHEME,
                    "3",
                    "TestOrganization201",
                    "RED_code_201",
                    "ticker_201",
                    "CountryC",
        Region.AFRICA.name(),
        Sector.BASICMATERIALS.name(),
        CreditRating.A.name(),
        CreditRating.A.name(),
        CreditRatingFitch.A.name(),
        CreditRatingMoodys.A.name(),
        CreditRatingStandardAndPoors.A.name(),
        0);
    template.update("INSERT INTO org_organisation VALUES (?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)",
                    202,
                    201,
                    toSqlTimestamp(_version2Instant),
                    MAX_SQL_TIMESTAMP,
                    toSqlTimestamp(_version2Instant),
                    MAX_SQL_TIMESTAMP,
                    PROVIDER_SCHEME,
                    "3",
                    "TestOrganization201",
                    "RED_code_201",
                    "ticker_201",
                    "CountryD",
        Region.AFRICA.name(),
        Sector.BASICMATERIALS.name(),
        CreditRating.B.name(),
        CreditRating.B.name(),
        CreditRatingFitch.B.name(),
        CreditRatingMoodys.B.name(),
        CreditRatingStandardAndPoors.B.name(),
        0);

    _totalOrganizations = 3;
  }

  //-------------------------------------------------------------------------
  protected void assert101(final OrganizationDocument test) {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization organization = test.getOrganization();
    assertNotNull(organization);
    assertEquals(uniqueId, organization.getUniqueId());
    assertEquals(ExternalId.of(PROVIDER_SCHEME, "1"), test.getProviderId());
    assertEquals("TestOrganization101", test.getOrganization().getObligor().getObligorShortName());
    assertEquals("RED_code_101", test.getOrganization().getObligor().getObligorREDCode());
    assertEquals("ticker_101", test.getOrganization().getObligor().getObligorTicker());
    assertEquals("CountryA", test.getOrganization().getObligor().getCountry());
    assertEquals(Region.AFRICA, test.getOrganization().getObligor().getRegion());
    assertEquals(Sector.BASICMATERIALS, test.getOrganization().getObligor().getSector());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(CreditRatingFitch.A, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(CreditRatingMoodys.A, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.A, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(false, test.getOrganization().getObligor().isHasDefaulted());
  }

  protected void assert102(final OrganizationDocument test) {
    UniqueId uniqueId = UniqueId.of("DbOrg", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization organization = test.getOrganization();
    assertNotNull(organization);
    assertEquals(uniqueId, organization.getUniqueId());
    assertEquals(ExternalId.of(PROVIDER_SCHEME, "2"), test.getProviderId());
    assertEquals("TestOrganization102", test.getOrganization().getObligor().getObligorShortName());
    assertEquals("RED_code_102", test.getOrganization().getObligor().getObligorREDCode());
    assertEquals("ticker_102", test.getOrganization().getObligor().getObligorTicker());
    assertEquals("CountryB", test.getOrganization().getObligor().getCountry());
    assertEquals(Region.AFRICA, test.getOrganization().getObligor().getRegion());
    assertEquals(Sector.BASICMATERIALS, test.getOrganization().getObligor().getSector());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(CreditRatingFitch.A, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(CreditRatingMoodys.A, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.A, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(false, test.getOrganization().getObligor().isHasDefaulted());
  }

  protected void assert201(final OrganizationDocument test) {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization organization = test.getOrganization();
    assertNotNull(organization);
    assertEquals(uniqueId, organization.getUniqueId());
    assertEquals(ExternalId.of(PROVIDER_SCHEME, "3"), test.getProviderId());
    assertEquals("TestOrganization201", test.getOrganization().getObligor().getObligorShortName());
    assertEquals("RED_code_201", test.getOrganization().getObligor().getObligorREDCode());
    assertEquals("ticker_201", test.getOrganization().getObligor().getObligorTicker());
    assertEquals("CountryC", test.getOrganization().getObligor().getCountry());
    assertEquals(Region.AFRICA, test.getOrganization().getObligor().getRegion());
    assertEquals(Sector.BASICMATERIALS, test.getOrganization().getObligor().getSector());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(CreditRatingFitch.A, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(CreditRatingMoodys.A, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.A, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(false, test.getOrganization().getObligor().isHasDefaulted());
  }

  protected void assert202(final OrganizationDocument test) {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization organization = test.getOrganization();
    assertNotNull(organization);
    assertEquals(uniqueId, organization.getUniqueId());
    assertEquals(ExternalId.of(PROVIDER_SCHEME, "3"), test.getProviderId());
    assertEquals("TestOrganization201", test.getOrganization().getObligor().getObligorShortName());
    assertEquals("RED_code_201", test.getOrganization().getObligor().getObligorREDCode());
    assertEquals("ticker_201", test.getOrganization().getObligor().getObligorTicker());
    assertEquals("CountryD", test.getOrganization().getObligor().getCountry());
    assertEquals(Region.AFRICA, test.getOrganization().getObligor().getRegion());
    assertEquals(Sector.BASICMATERIALS, test.getOrganization().getObligor().getSector());
    assertEquals(CreditRating.B, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(CreditRating.B, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(CreditRatingFitch.B, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(CreditRatingMoodys.B, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.B, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(false, test.getOrganization().getObligor().isHasDefaulted());
  }

}
