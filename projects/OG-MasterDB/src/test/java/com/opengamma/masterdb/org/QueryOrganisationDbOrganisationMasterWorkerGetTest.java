/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.org;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.util.test.DbTest;

/**
 * Tests QueryOrganisationDbOrganisationMasterWorker.
 */
public class QueryOrganisationDbOrganisationMasterWorkerGetTest extends AbstractDbOrganisationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryOrganisationDbOrganisationMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryOrganisationDbOrganisationMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getOrganisation_nullUID() {
    _orgMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganisation_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
    _orgMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganisation_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "1");
    _orgMaster.get(uniqueId);
  }

  @Test
  public void test_getOrganisation_versioned_oneOrganisationDate() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    OrganisationDocument test = _orgMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getOrganisation_versioned_twoOrganisationDates() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "102", "0");
    OrganisationDocument test = _orgMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getOrganisation_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "0");
    OrganisationDocument test = _orgMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getOrganisation_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "1");
    OrganisationDocument test = _orgMaster.get(uniqueId);
    assert202(test);
  }


  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganisation_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0");
    _orgMaster.get(uniqueId);
  }

  @Test
  public void test_getOrganisation_unversioned() {
    UniqueId oid = UniqueId.of("DbOrg", "201");
    OrganisationDocument test = _orgMaster.get(oid);
    assert202(test);
  }

}
