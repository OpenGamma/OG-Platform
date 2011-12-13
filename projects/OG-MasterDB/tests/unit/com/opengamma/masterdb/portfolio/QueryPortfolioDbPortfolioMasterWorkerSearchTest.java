/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
public class QueryPortfolioDbPortfolioMasterWorkerSearchTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPortfolioDbPortfolioMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents_maxDepth() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(-1);
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_documents_depthZero() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(0);
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 0);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_documents_depthOne() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(1);
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 1);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_portfolioIds_none() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPortfolioObjectIds(new ArrayList<ObjectId>());
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_portfolioIds_one() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "9999"));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioIds_two() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "101"));
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_portfolioIds_badSchemeValidOid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("Rubbish", "201"));
    _prtMaster.search(request);
  }

  @Test
  public void test_search_nodeIds_none() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setNodeObjectIds(new ArrayList<ObjectId>());
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_nodeIds() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "9999"));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_nodeIds_badSchemeValidOid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("Rubbish", "211"));
    _prtMaster.search(request);
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchSome() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchNone() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "101"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("FooBar");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name_exactMatch() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio101");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_case() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio101");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_wildcardMatch_one() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio2*");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcardMatch_two() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio1*");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio1*");
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_visibility() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVisibility(DocumentVisibility.HIDDEN);
    PortfolioSearchResult test = _prtMaster.search(request);
    
    assertEquals(_totalPortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
    assert301(test.getDocuments().get(3));
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_prtMaster.getClass().getSimpleName() + "[DbPrt]", _prtMaster.toString());
  }

}
