/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityHistoryRequest;
import com.opengamma.financial.security.master.SecurityHistoryResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerHistoryTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerHistoryTest.class);

  private DbSecurityMasterWorker _worker;

  public QuerySecurityDbSecurityMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
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
  @Test
  public void test_history_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);  // new version
    SecurityDocument doc1 = test.getDocuments().get(1);  // old version
    
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(doc0.getVersionFromInstant(), doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    DefaultSecurity security0 = doc0.getSecurity();
    assertNotNull(security0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), security0.getUniqueIdentifier());
    assertEquals("TestSecurity202", security0.getName());
    assertEquals("EQUITY", security0.getSecurityType());
    IdentifierBundle idKey0 = security0.getIdentifiers();
    assertNotNull(idKey0);
    assertEquals(1, idKey0.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), idKey0.getIdentifiers().iterator().next());
    
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc1.getSecurityId());
    assertNotNull(doc1.getVersionFromInstant());
    assertEquals(doc0.getVersionFromInstant(), doc1.getVersionToInstant());
    assertEquals(doc1.getVersionFromInstant(), doc1.getCorrectionFromInstant());
    assertEquals(null, doc1.getCorrectionToInstant());
    DefaultSecurity security1 = doc1.getSecurity();
    assertNotNull(security1);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), security1.getUniqueIdentifier());
    assertEquals("TestSecurity201", security1.getName());
    assertEquals("EQUITY", security1.getSecurityType());
    IdentifierBundle idKey1 = security1.getIdentifiers();
    assertNotNull(idKey1);
    assertEquals(1, idKey1.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), idKey1.getIdentifiers().iterator().next());
  }

  @Test
  public void test_history_documentCountWhenMultipleSecurities() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "102");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);  // new version
    
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc0.getSecurityId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(doc0.getVersionFromInstant(), doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    DefaultSecurity security = doc0.getSecurity();
    assertNotNull(security);
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), security.getUniqueIdentifier());
    assertEquals("TestSecurity102", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    IdentifierBundle idKey = security.getIdentifiers();
    assertNotNull(idKey);
    assertEquals(2, idKey.size());
    assertTrue(idKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertTrue(idKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc1.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    SecurityHistoryResult test = _worker.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc0.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc1.getSecurityId());
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc1.getSecurityId());
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc0.getSecurityId());
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbSec", "201");
    SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    SecurityHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc1.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
