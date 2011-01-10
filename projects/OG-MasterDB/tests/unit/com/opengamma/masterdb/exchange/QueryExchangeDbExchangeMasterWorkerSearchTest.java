/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryExchangeDbExchangeMasterWorker.
 */
public class QueryExchangeDbExchangeMasterWorkerSearchTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorkerSearchTest.class);

  private DbExchangeMasterWorker _worker;

  public QueryExchangeDbExchangeMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryExchangeDbExchangeMasterWorker();
    _worker.init(_exgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());
    
    assertEquals(_totalExchanges, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 2));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("FooBar");
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TestExchange102");
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TESTExchange102");
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TestExchange1*");
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TESTExchange1*");
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_exchangeIds_none() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExchangeIds(new ArrayList<UniqueIdentifier>());
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchangeIds() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeId(UniqueIdentifier.of("DbExg", "101"));
    request.addExchangeId(UniqueIdentifier.of("DbExg", "201"));
    request.addExchangeId(UniqueIdentifier.of("DbExg", "9999"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_search_exchangeIds_badSchemeValidOid() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeId(UniqueIdentifier.of("Rubbish", "120"));
    _worker.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExchangeKeys(new IdentifierSearch());
    request.getExchangeKeys().setSearchType(IdentifierSearchType.EXACT);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExchangeKeys(new IdentifierSearch());
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExchangeKeys(new IdentifierSearch());
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ANY);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExchangeKeys(new IdentifierSearch());
    request.getExchangeKeys().setSearchType(IdentifierSearchType.NONE);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(_totalExchanges, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("A", "B"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("C", "D"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("E", "F"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GH() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("G", "H"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("A", "H"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GH() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("E", "F"), Identifier.of("G", "H"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("E", "H"), Identifier.of("A", "D"));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("A", "B"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("C", "D"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("E", "F"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GH() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("G", "H"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("A", "H"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "H")));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GH() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F"), Identifier.of("A", "H")));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("A", "B"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.NONE);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(Identifier.of("C", "D"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.NONE);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.EXACT);
    ExchangeSearchResult test = _worker.search(request);
    
    System.out.println(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GH() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.EXACT);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
    request.getExchangeKeys().setSearchType(IdentifierSearchType.EXACT);
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_oneId_AB() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("A", "B"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_oneId_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_oneId_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("A", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_twoIds_AB_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_CD_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("C", "D"), Identifier.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("C", "D"), Identifier.of("E", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_threeIds_AB_CD_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_AB_CD_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(Identifier.of("C", "D"), Identifier.of("E", "F"), Identifier.of("A", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_ids_AB_or_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("A", "B"));
//    request.addExchangeKey(Identifier.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_EF_or_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("E", "F"));
//    request.addExchangeKey(Identifier.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_or_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(Identifier.of("E", "H"));
//    request.addExchangeKey(Identifier.of("A", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//    
//    assertEquals(0, test.getDocuments().size());
//  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    ExchangeSearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
