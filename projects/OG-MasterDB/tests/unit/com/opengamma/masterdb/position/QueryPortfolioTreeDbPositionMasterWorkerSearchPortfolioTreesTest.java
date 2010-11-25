/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PortfolioTreeSearchRequest;
import com.opengamma.master.position.PortfolioTreeSearchResult;
import com.opengamma.masterdb.position.QueryPortfolioTreeDbPositionMasterWorker;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreesTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreesTest.class);

  private QueryPortfolioTreeDbPositionMasterWorker _worker;

  public QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreesTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPortfolioTreeDbPositionMasterWorker();
    _worker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents_maxDepth() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setDepth(-1);
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(_totalPortfolios, test.getDocuments().size());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(uid, doc0.getUniqueId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    ManageablePortfolio portfolio = doc0.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio101", portfolio.getName());
    
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode.getUniqueIdentifier());
    assertEquals("TestNode111", rootNode.getName());
    assertEquals(1, rootNode.getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "0"), rootNode.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode112", rootNode.getChildNodes().get(0).getName());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "113", "0"), rootNode.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode113", rootNode.getChildNodes().get(0).getChildNodes().get(0).getName());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size());
    
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc1.getUniqueId());
  }

  @Test
  public void test_search_documents_depthZero() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    assertEquals(_totalPortfolios, test.getDocuments().size());
    
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
    ManageablePortfolioNode rootNode0 = doc0.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode0.getUniqueIdentifier());
    assertEquals(0, rootNode0.getChildNodes().size());
    
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc1.getUniqueId());
    ManageablePortfolioNode rootNode1 = doc1.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "211", "1"), rootNode1.getUniqueIdentifier());
    assertEquals(0, rootNode1.getChildNodes().size());
  }

  @Test
  public void test_search_documents_depthOne() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setDepth(1);
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    assertEquals(_totalPortfolios, test.getDocuments().size());
    
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
    ManageablePortfolioNode rootNode0 = doc0.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode0.getUniqueIdentifier());
    assertEquals(1, rootNode0.getChildNodes().size());
    assertEquals(0, rootNode0.getChildNodes().get(0).getChildNodes().size());
    
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc1.getUniqueId());
    ManageablePortfolioNode rootNode1 = doc1.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "211", "1"), rootNode1.getUniqueIdentifier());
    assertEquals(0, rootNode1.getChildNodes().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 1));
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
  }

  @Test
  public void test_search_pageTwo() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 1));
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(_totalPortfolios, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("FooBar");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name_exactMatch() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("TestPortfolio101");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
  }

  @Test
  public void test_search_name_case() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("TESTPortfolio101");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
  }

  @Test
  public void test_search_name_wildcardMatch_one() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("TestPortfolio1*");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
  }

  @Test
  public void test_search_name_wildcardMatch_two() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("TestPort*");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc1.getUniqueId());
  }

  @Test
  public void test_search_name_wildcardCase() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setName("TESTPortfolio1*");
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    PortfolioTreeSearchResult test = _worker.searchPortfolioTrees(request);
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc1.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
