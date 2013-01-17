/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
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

/**
 * Tests ModifyOrganisationDbOrganisationMasterWorker.
 */
public class ModifyOrganisationDbOrganisationMasterWorkerRemoveTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganisationDbOrganisationMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganisationDbOrganisationMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeOrganisation_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
    _orgMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_orgMaster.getTimeSource());

    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    _orgMaster.remove(uniqueId);
    OrganisationDocument test = _orgMaster.get(uniqueId);

    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableOrganisation organisation = test.getOrganisation();
    assertNotNull(organisation);
    assertEquals(uniqueId, organisation.getUniqueId());
    assertEquals("TestOrganisation101", test.getOrganisation().getObligor().getObligorShortName());
    assertEquals("RED_code_101", test.getOrganisation().getObligor().getObligorREDCode());
    assertEquals("ticker_101", test.getOrganisation().getObligor().getObligorTicker());

    assertEquals(CreditRatingFitch.A, test.getOrganisation().getObligor().getFitchCreditRating());
    assertEquals(CreditRating.A, test.getOrganisation().getObligor().getCompositeRating());
    assertEquals(CreditRating.A, test.getOrganisation().getObligor().getImpliedRating());
    assertEquals(CreditRatingMoodys.A, test.getOrganisation().getObligor().getMoodysCreditRating());
    assertEquals(CreditRatingStandardAndPoors.A, test.getOrganisation().getObligor().getStandardAndPoorsCreditRating());
    assertEquals("CountryA", test.getOrganisation().getObligor().getCountry());
    assertEquals(Sector.BASICMATERIALS, test.getOrganisation().getObligor().getSector());
    assertEquals(Region.AFRICA, test.getOrganisation().getObligor().getRegion());
    assertEquals(false, test.getOrganisation().getObligor().isHasDefaulted());
  }

}
