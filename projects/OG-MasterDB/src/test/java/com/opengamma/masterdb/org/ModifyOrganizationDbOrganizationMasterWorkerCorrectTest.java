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
public class ModifyOrganizationDbOrganizationMasterWorkerCorrectTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganizationDbOrganizationMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganizationDbOrganizationMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctOrganization_nullDocument() {
    _orgMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noOrganizationId() {
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
    OrganizationDocument doc = new OrganizationDocument(org);
    doc.setUniqueId(null);
    _orgMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noOrganization() {
    OrganizationDocument doc = new OrganizationDocument();
    doc.setUniqueId(UniqueId.of("DbOrg", "101", "0"));
    _orgMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
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
    _orgMaster.correct(doc);
  }


  @Test
  public void test_correct_getUpdateGet() {
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

    OrganizationDocument corrected = _orgMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getOrganization(), corrected.getOrganization());

    OrganizationDocument old = _orgMaster.get(UniqueId.of("DbOrg", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getOrganization(), old.getOrganization());

    OrganizationHistoryRequest search = new OrganizationHistoryRequest(base.getUniqueId(), now, null);
    OrganizationHistoryResult searchResult = _orgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

}
