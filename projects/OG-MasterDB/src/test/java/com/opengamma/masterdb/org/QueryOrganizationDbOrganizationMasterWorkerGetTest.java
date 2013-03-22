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
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryOrganizationDbOrganizationMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryOrganizationDbOrganizationMasterWorkerGetTest extends AbstractDbOrganizationMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryOrganizationDbOrganizationMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryOrganizationDbOrganizationMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getOrganization_nullUID() {
    _orgMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganization_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0", "0");
    _orgMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganization_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "1");
    _orgMaster.get(uniqueId);
  }

  @Test
  public void test_getOrganization_versioned_oneOrganizationDate() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "101", "0");
    OrganizationDocument test = _orgMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getOrganization_versioned_twoOrganizationDates() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "102", "0");
    OrganizationDocument test = _orgMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getOrganization_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "0");
    OrganizationDocument test = _orgMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getOrganization_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "201", "1");
    OrganizationDocument test = _orgMaster.get(uniqueId);
    assert202(test);
  }


  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getOrganization_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbOrg", "0");
    _orgMaster.get(uniqueId);
  }

  @Test
  public void test_getOrganization_unversioned() {
    UniqueId oid = UniqueId.of("DbOrg", "201");
    OrganizationDocument test = _orgMaster.get(oid);
    assert202(test);
  }

}
