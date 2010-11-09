/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
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
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
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
    assert202(test.getDocuments().get(0));
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
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity102");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity1*");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity1*");
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
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
  public void test_search_oneId_AB() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("A", "B"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneId_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("C", "D"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneId_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("E", "F"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneId_GH() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("G", "H"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneId_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("A", "H"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoIds_AB_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoIds_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoIds_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "H")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeIds_AB_CD_EF() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeIds_AB_CD_GH() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeIds_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F"), Identifier.of("A", "H")));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_ids_AB_or_CD() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("A", "B"));
    request.addIdentifierBundle(Identifier.of("C", "D"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_ids_EF_or_GH() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("E", "F"));
    request.addIdentifierBundle(Identifier.of("G", "H"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_ids_or_noMatch() {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addIdentifierBundle(Identifier.of("E", "H"));
    request.addIdentifierBundle(Identifier.of("A", "D"));
    SecuritySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
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
