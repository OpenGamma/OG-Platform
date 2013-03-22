/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyOrganizationDbOrganizationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyOrganizationDbOrganizationMasterWorkerUpdateTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganizationDbOrganizationMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganizationDbOrganizationMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateOrganization_nullDocument() {
    _orgMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noOrganizationId() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101");
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
    org.setUniqueId(uniqueId);
    OrganizationDocument doc = new OrganizationDocument();
    doc.setOrganization(org);
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noOrganization() {
    OrganizationDocument doc = new OrganizationDocument();
    doc.setUniqueId(UniqueId.of("DbOrg", "101", "0"));
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
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
    org.setUniqueId(uniqueId);
    OrganizationDocument doc = new OrganizationDocument(org);
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "0");
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
    org.setUniqueId(uniqueId);
    OrganizationDocument doc = new OrganizationDocument(org);
    _orgMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_orgMaster.getClock());

    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    OrganizationDocument base = _orgMaster.get(uniqueId);
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
    org.setUniqueId(uniqueId);
    OrganizationDocument input = new OrganizationDocument(org);

    OrganizationDocument updated = _orgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getOrganization(), updated.getOrganization());

    OrganizationDocument old = _orgMaster.get(uniqueId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getOrganization(), old.getOrganization());

    OrganizationHistoryRequest search = new OrganizationHistoryRequest(base.getUniqueId(), null, now);
    OrganizationHistoryResult searchResult = _orgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

}
