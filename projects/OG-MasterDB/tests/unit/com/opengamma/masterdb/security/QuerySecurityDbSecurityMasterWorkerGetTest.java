/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.security.DbSecurityMasterWorker;
import com.opengamma.masterdb.security.QuerySecurityDbSecurityMasterWorker;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerGetTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerGetTest.class);

  private DbSecurityMasterWorker _worker;

  public QuerySecurityDbSecurityMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QuerySecurityDbSecurityMasterWorker();
    _worker.init(_secMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getSecurity_nullUID() {
    _worker.get(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getSecurity_versioned_oneSecurityKey() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    SecurityDocument test = _worker.get(uid);
    assert101(test);
  }

  @Test
  public void test_getSecurity_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "102", "0");
    SecurityDocument test = _worker.get(uid);
    assert102(test);
  }

  @Test
  public void test_getSecurity_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
    SecurityDocument test = _worker.get(uid);
    assert201(test);
  }

  @Test
  public void test_getSecurity_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "1");
    SecurityDocument test = _worker.get(uid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getSecurity_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "0");
    _worker.get(uid);
  }

  @Test
  public void test_getSecurity_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityDocument test = _worker.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
