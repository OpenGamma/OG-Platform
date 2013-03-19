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
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyOrganisationDbOrganisationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyOrganisationDbOrganisationMasterWorkerAddTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganisationDbOrganisationMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganisationDbOrganisationMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addOrganisation_nullDocument() {
    _orgMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noOrganisation() {
    OrganisationDocument doc = new OrganisationDocument();
    _orgMaster.add(doc);
  }

  @Test
  public void test_add_organisation() {
    Instant now = Instant.now(_orgMaster.getClock());

    ManageableOrganisation org = new ManageableOrganisation("TestOrganisation101",
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
    OrganisationDocument doc = new OrganisationDocument(org);
    String shortName = doc.getOrganisation().getObligor().getObligorShortName();

    String redCode = doc.getOrganisation().getObligor().getObligorREDCode();
    String ticker = doc.getOrganisation().getObligor().getObligorTicker();

    CreditRatingFitch creditRatingFitch = doc.getOrganisation().getObligor().getFitchCreditRating();
    CreditRating compositeCreditRating = doc.getOrganisation().getObligor().getCompositeRating();
    CreditRating impliedCreditRating = doc.getOrganisation().getObligor().getImpliedRating();
    CreditRatingMoodys creditRatingMoodys = doc.getOrganisation().getObligor().getMoodysCreditRating();
    CreditRatingStandardAndPoors creditRatingStandardAndPoors = doc.getOrganisation().getObligor().getStandardAndPoorsCreditRating();

    String country = doc.getOrganisation().getObligor().getCountry();
    Sector sector = doc.getOrganisation().getObligor().getSector();
    Region region = doc.getOrganisation().getObligor().getRegion();
    boolean hasDefaulted = doc.getOrganisation().getObligor().isHasDefaulted();

    OrganisationDocument test = _orgMaster.add(doc);

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
    ManageableOrganisation testOrganisation = test.getOrganisation();
    assertNotNull(testOrganisation);
    assertEquals(uniqueId, testOrganisation.getUniqueId());

    assertEquals(shortName, test.getOrganisation().getObligor().getObligorShortName());
    assertEquals(redCode, test.getOrganisation().getObligor().getObligorREDCode());
    assertEquals(ticker, test.getOrganisation().getObligor().getObligorTicker());

    assertEquals(creditRatingFitch, test.getOrganisation().getObligor().getFitchCreditRating());
    assertEquals(compositeCreditRating, test.getOrganisation().getObligor().getCompositeRating());
    assertEquals(impliedCreditRating, test.getOrganisation().getObligor().getImpliedRating());
    assertEquals(creditRatingMoodys, test.getOrganisation().getObligor().getMoodysCreditRating());
    assertEquals(creditRatingStandardAndPoors, test.getOrganisation().getObligor().getStandardAndPoorsCreditRating());
    assertEquals(country, test.getOrganisation().getObligor().getCountry());
    assertEquals(sector, test.getOrganisation().getObligor().getSector());
    assertEquals(region, test.getOrganisation().getObligor().getRegion());
    assertEquals(hasDefaulted, test.getOrganisation().getObligor().isHasDefaulted());

  }

  @Test
  public void test_add_addThenGet() {
    ManageableOrganisation org = new ManageableOrganisation("TestOrganisation101",
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
    OrganisationDocument doc = new OrganisationDocument(org);
    OrganisationDocument added = _orgMaster.add(doc);

    OrganisationDocument test = _orgMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingProperties() {

    OrganisationDocument doc = new OrganisationDocument();
    ManageableOrganisation org = new ManageableOrganisation();
    doc.setOrganisation(org);
    OrganisationDocument added = _orgMaster.add(doc);
  }


}
