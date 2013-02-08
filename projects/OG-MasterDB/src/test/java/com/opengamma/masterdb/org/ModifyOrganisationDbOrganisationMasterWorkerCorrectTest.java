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
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyOrganisationDbOrganisationMasterWorker.
 */
public class ModifyOrganisationDbOrganisationMasterWorkerCorrectTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganisationDbOrganisationMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganisationDbOrganisationMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctOrganisation_nullDocument() {
    _orgMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noOrganisationId() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101");
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
    org.setUniqueId(uniqueId);
    OrganisationDocument doc = new OrganisationDocument(org);
    doc.setUniqueId(null);
    _orgMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noOrganisation() {
    OrganisationDocument doc = new OrganisationDocument();
    doc.setUniqueId(UniqueId.of("DbOrg", "101", "0"));
    _orgMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
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
    org.setUniqueId(uniqueId);
    OrganisationDocument doc = new OrganisationDocument(org);
    _orgMaster.correct(doc);
  }


  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_orgMaster.getClock());

    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    OrganisationDocument base = _orgMaster.get(uniqueId);
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
    org.setUniqueId(uniqueId);
    OrganisationDocument input = new OrganisationDocument(org);

    OrganisationDocument corrected = _orgMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getOrganisation(), corrected.getOrganisation());

    OrganisationDocument old = _orgMaster.get(UniqueId.of("DbOrg", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getOrganisation(), old.getOrganisation());

    OrganisationHistoryRequest search = new OrganisationHistoryRequest(base.getUniqueId(), now, null);
    OrganisationHistoryResult searchResult = _orgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

}
