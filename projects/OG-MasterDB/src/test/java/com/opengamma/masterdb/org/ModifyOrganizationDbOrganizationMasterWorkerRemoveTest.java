/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
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
public class ModifyOrganizationDbOrganizationMasterWorkerRemoveTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganizationDbOrganizationMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganizationDbOrganizationMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeOrganization_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
    _orgMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_orgMaster.getClock());

    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    _orgMaster.remove(uniqueId);
    OrganizationDocument test = _orgMaster.get(uniqueId);

    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganization Organization = test.getOrganization();
    assertNotNull(Organization);
    assertEquals(uniqueId, Organization.getUniqueId());
    assertEquals("TestOrganization101", test.getOrganization().getObligor().getObligorShortName());
    assertEquals("RED_code_101", test.getOrganization().getObligor().getObligorREDCode());
    assertEquals("ticker_101", test.getOrganization().getObligor().getObligorTicker());

    assertEquals(CreditRatingFitch.A, test.getOrganization().getObligor().getFitchCreditRating());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getCompositeRating());
    assertEquals(CreditRating.A, test.getOrganization().getObligor().getImpliedRating());
    assertEquals(CreditRatingMoodys.A, test.getOrganization().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.A, test.getOrganization().getObligor().getStandardAndPoorsCreditRating());
    assertEquals("CountryA", test.getOrganization().getObligor().getCountry());
    assertEquals(Sector.BASICMATERIALS, test.getOrganization().getObligor().getSector());
    assertEquals(Region.AFRICA, test.getOrganization().getObligor().getRegion());
    assertEquals(false, test.getOrganization().getObligor().isHasDefaulted());
  }

}
