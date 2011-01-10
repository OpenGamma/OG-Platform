/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
public class QueryPortfolioDbPortfolioMasterWorkerSearchTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerSearchTest.class);

  private QueryPortfolioDbPortfolioMasterWorker _worker;

  public QueryPortfolioDbPortfolioMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPortfolioDbPortfolioMasterWorker();
    _worker.init(_prtMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents_maxDepth() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(-1);
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(_totalPortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_documents_depthZero() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(0);
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    assertEquals(_totalPortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 0);
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_documents_depthOne() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(1);
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    assertEquals(_totalPortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 1);
    assert202(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 1));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_pageTwo() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 1));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_portfolioIds_none() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPortfolioIds(new ArrayList<UniqueIdentifier>());
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_portfolioIds_one() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "201"));
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "9999"));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioIds_two() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "101"));
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "201"));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_search_portfolioIds_badSchemeValidOid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("Rubbish", "201"));
    _worker.search(request);
  }

  @Test
  public void test_search_nodeIds_none() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setNodeIds(new ArrayList<UniqueIdentifier>());
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_nodeIds() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addNodeId(UniqueIdentifier.of("DbPrt", "211"));
    request.addNodeId(UniqueIdentifier.of("DbPrt", "9999"));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_search_nodeIds_badSchemeValidOid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("Rubbish", "211"));
    _worker.search(request);
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchSome() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "201"));
    request.addNodeId(UniqueIdentifier.of("DbPrt", "211"));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchNone() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioId(UniqueIdentifier.of("DbPrt", "101"));
    request.addNodeId(UniqueIdentifier.of("DbPrt", "211"));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("FooBar");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name_exactMatch() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio101");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_case() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio101");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_wildcardMatch_one() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio1*");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_wildcardMatch_two() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPort*");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio1*");
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert201(test.getDocuments().get(1));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    PortfolioSearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPrt]", _worker.toString());
  }

}
