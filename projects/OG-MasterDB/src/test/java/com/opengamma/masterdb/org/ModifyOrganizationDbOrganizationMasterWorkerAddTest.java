/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyOrganizationDbOrganizationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyOrganizationDbOrganizationMasterWorkerAddTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganizationDbOrganizationMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganizationDbOrganizationMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addOrganization_nullDocument() {
    _orgMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noOrganization() {
    OrganizationDocument doc = new OrganizationDocument();
    _orgMaster.add(doc);
  }

  @Test
  public void test_add_Organization() {
    Instant now = Instant.now(_orgMaster.getClock());

    ManageableOrganization org = new ManageableOrganization("TestOrganization101",
                                                            "RED_code_101",
                                                            "ticker_101",
                                                            Region.AFRICA,
                                                            "CountryA",
                                                            Sector.BASICMATERIALS,
                                                            CreditRating.A,
                                                            CreditRating.A,
                                                            CreditRatingFitch.A,
                                                            CreditRatingMoodys.A,
                                                            CreditRatingStandardAndPoors.A,
                                                            false);
    OrganizationDocument doc = new OrganizationDocument(org);
    String shortName = doc.getOrganization().getObligor().getObligorShortName();

    String redCode = doc.getOrganization().getObligor().getObligorREDCode();
    String ticker = doc.getOrganization().getObligor().getObligorTicker();

    CreditRatingFitch creditRatingFitch = doc.getOrganization().getObligor().getFitchCreditRating();
    CreditRating compositeCreditRating = doc.getOrganization().getObligor().getCompositeRating();
    CreditRating impliedCreditRating = doc.getOrganization().getObligor().getImpliedRating();
    CreditRatingMoodys creditRatingMoodys = doc.getOrganization().getObligor().getMoodysCreditRating();
    CreditRatingStandardAndPoors creditRatingStandardAndPoors = doc.getOrganization().getObligor().getStandardAndPoorsCreditRating();

    String country = doc.getOrganization().getObligor().getCountry();
    Sector sector = doc.getOrganization().getObligor().getSector();
    Region region = doc.getOrganization().getObligor().getRegion();
    boolean hasDefaulted = doc.getOrganization().getObligor().isHasDefaulted();

    OrganizationDocument test = _orgMaster.add(doc);

    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbOrg", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization testOrganization = test.getOrganization();
    assertNotNull(testOrganization);
    assertEquals(uniqueId, testOrganization.getUniqueId());

    assertEquals(shortName, test.getOrganization().getObligor().getObligorShortName());
    assertEquals(redCode, test.getOrganization().getObligor().getObligorREDCode());
    assertEquals(ticker, test.getOrganization().getObligor().getObligorTicker());

    assertEquals(creditRatingFitch, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(compositeCreditRating, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(impliedCreditRating, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(creditRatingMoodys, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(creditRatingStandardAndPoors, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(country, test.getOrganization().getObligor().getCountry());
    assertEquals(sector, test.getOrganization().getObligor().getSector());
    assertEquals(region, test.getOrganization().getObligor().getRegion());
    assertEquals(hasDefaulted, test.getOrganization().getObligor().isHasDefaulted());

  }

  @Test
  public void test_add_addThenGet() {
    ManageableOrganization org = new ManageableOrganization("TestOrganization101",
                                                            "RED_code_101",
                                                            "ticker_101",
                                                            Region.AFRICA,
                                                            "CountryA",
                                                            Sector.BASICMATERIALS,
                                                            CreditRating.A,
                                                            CreditRating.A,
                                                            CreditRatingFitch.A,
                                                            CreditRatingMoodys.A,
                                                            CreditRatingStandardAndPoors.A,
                                                            false);
    OrganizationDocument doc = new OrganizationDocument(org);
    OrganizationDocument added = _orgMaster.add(doc);

    OrganizationDocument test = _orgMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingProperties() {

    OrganizationDocument doc = new OrganizationDocument();
    ManageableOrganization org = new ManageableOrganization();
    doc.setOrganization(org);
    _orgMaster.add(doc);
  }


}
