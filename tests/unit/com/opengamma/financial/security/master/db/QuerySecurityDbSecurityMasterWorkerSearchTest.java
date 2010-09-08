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
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.financial.security.master.SecuritySearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QuerySecurityDbSecurityMasterWorker.
 */
public class QuerySecurityDbSecurityMasterWorkerSearchTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorkerSearchTest.class);

  private DbSecurityMasterWorker _worker;

  public QuerySecurityDbSecurityMasterWorkerSearchTest(String databaseType, String databaseVersion) {
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
  public void test_searchSecurities_documents() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(_totalSecurities, test.getDocuments().size());
    SecurityDocument doc1 = test.getDocuments().get(1);
    
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
    assertEquals(_version1Instant, doc1.getVersionFromInstant());
    assertEquals(null, doc1.getVersionToInstant());
    assertEquals(_version1Instant, doc1.getCorrectionFromInstant());
    assertEquals(null, doc1.getCorrectionToInstant());
    DefaultSecurity security = doc1.getSecurity();
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
  public void test_search_pageOne() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(new PagingRequest(1, 2));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
  }

  @Test
  public void test_search_pageTwo() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc0.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("FooBar");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity102");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc0.getSecurityId());
  }

  @Test
  public void test_search_name_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity102");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc0.getSecurityId());
  }

  @Test
  public void test_search_name_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity1*");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
  }

  @Test
  public void test_search_name_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity1*");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType("EQUITY");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc2.getSecurityId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_id_emptyBundle() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.EMPTY);
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_id_oneOfOne_101() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.of(Identifier.of("TICKER", "ORCL")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(_worker.get(UniqueIdentifier.of("DbSec", "101", "0")), doc0);
  }

  @Test
  public void test_search_id_oneOfOneLatest_201() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.of(Identifier.of("TICKER", "IBMC")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(_worker.get(UniqueIdentifier.of("DbSec", "201", "1")), doc0);
  }

  @Test
  public void test_search_id_oneOfTwo_102() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.of(Identifier.of("TICKER", "MSFT")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(_worker.get(UniqueIdentifier.of("DbSec", "102", "0")), doc0);
  }

  @Test
  public void test_search_id_twoOfTwo_102() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.of(Identifier.of("TICKER", "MSFT"), Identifier.of("NASDAQ", "Micro")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(_worker.get(UniqueIdentifier.of("DbSec", "102", "0")), doc0);
  }

  @Test
  public void test_search_id_twoOfTwoWhereOneMatches_102() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentityKey(IdentifierBundle.of(Identifier.of("TICKER", "MSFT"), Identifier.of("RUBBISH", "Micro")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    assertEquals(_worker.get(UniqueIdentifier.of("DbSec", "102", "0")), doc0);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "0"), doc2.getSecurityId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    SecurityDocument doc0 = test.getDocuments().get(0);
    SecurityDocument doc1 = test.getDocuments().get(1);
    SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbSec", "101", "0"), doc0.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "102", "0"), doc1.getSecurityId());
    assertEquals(UniqueIdentifier.of("DbSec", "201", "1"), doc2.getSecurityId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbSec]", _worker.toString());
  }

}
