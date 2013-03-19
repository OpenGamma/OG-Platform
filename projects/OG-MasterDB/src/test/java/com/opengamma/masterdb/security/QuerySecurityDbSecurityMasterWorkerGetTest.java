/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QuerySecurityDbSecurityMasterWorkerGetTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QuerySecurityDbSecurityMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getSecurity_nullUID() {
    _secMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    _secMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbSec", "101", "1");
    _secMaster.get(uniqueId);
  }

  @Test
  public void test_getSecurity_versioned_oneSecurityKey() {
    UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    SecurityDocument test = _secMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getSecurity_versioned_twoSecurityKeys() {
    UniqueId uniqueId = UniqueId.of("DbSec", "102", "0");
    SecurityDocument test = _secMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getSecurity_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbSec", "201", "0");
    SecurityDocument test = _secMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getSecurity_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbSec", "201", "1");
    SecurityDocument test = _secMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbSec", "0");
    _secMaster.get(uniqueId);
  }

  @Test
  public void test_getSecurity_unversioned() {
    UniqueId oid = UniqueId.of("DbSec", "201");
    SecurityDocument test = _secMaster.get(oid);
    assert202(test);
  }

}
