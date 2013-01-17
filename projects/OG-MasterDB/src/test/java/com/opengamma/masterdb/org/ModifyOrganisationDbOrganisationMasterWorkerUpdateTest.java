/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.masterdb.orgs.DbOrganisationMaster;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyOrganisationDbOrganisationMasterWorker.
 */
public class ModifyOrganisationDbOrganisationMasterWorkerUpdateTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyOrganisationDbOrganisationMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyOrganisationDbOrganisationMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateOrganisation_nullDocument() {
    _orgMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noOrganisationId() {
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
    OrganisationDocument doc = new OrganisationDocument();
    doc.setOrganisation(org);
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noOrganisation() {
    OrganisationDocument doc = new OrganisationDocument();
    doc.setUniqueId(UniqueId.of("DbOrg", "101", "0"));
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
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
    _orgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "0");
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
    _orgMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_orgMaster.getTimeSource());

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

    OrganisationDocument updated = _orgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getOrganisation(), updated.getOrganisation());

    OrganisationDocument old = _orgMaster.get(uniqueId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getOrganisation(), old.getOrganisation());

    OrganisationHistoryRequest search = new OrganisationHistoryRequest(base.getUniqueId(), null, now);
    OrganisationHistoryResult searchResult = _orgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

}
