/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

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
    
    assertNotNull(test);
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    DefaultSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), idKey.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getSecurity_versioned_twoSecurityKeys() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "102", "0");
    SecurityDocument test = _worker.get(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    DefaultSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity102", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(2, idKey.size());
    assertEquals(true, idKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, idKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
  }

  @Test
  public void test_getSecurity_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
    SecurityDocument test = _worker.get(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    DefaultSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity201", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), idKey.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getSecurity_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "1");
    SecurityDocument test = _worker.get(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    DefaultSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity202", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), idKey.getIdentifiers().iterator().next());
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
    
    assertNotNull(test);
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "1");
    assertEquals(uid, test.getSecurityId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    DefaultSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity202", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), idKey.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
