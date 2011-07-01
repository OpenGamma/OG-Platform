/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerGetTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public QuerySecurityDbSecurityMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getSecurity_nullUID() {
    _secMaster.get(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0", "0");
    _secMaster.get(uid);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "1");
    _secMaster.get(uid);
  }

  @Test
  public void test_getSecurity_versioned_oneSecurityKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    SecurityDocument test = _secMaster.get(uid);
    assert101(test);
  }

  @Test
  public void test_getSecurity_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "102", "0");
    SecurityDocument test = _secMaster.get(uid);
    assert102(test);
  }

  @Test
  public void test_getSecurity_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
    SecurityDocument test = _secMaster.get(uid);
    assert201(test);
  }

  @Test
  public void test_getSecurity_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "1");
    SecurityDocument test = _secMaster.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0");
    _secMaster.get(uid);
  }

  @Test
  public void test_getSecurity_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityDocument test = _secMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_secMaster.getClass().getSimpleName() + "[DbSec]", _secMaster.toString());
  }

}
