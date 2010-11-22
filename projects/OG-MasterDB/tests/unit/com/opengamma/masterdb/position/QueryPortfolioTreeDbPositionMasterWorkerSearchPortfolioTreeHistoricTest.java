/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.master.position.PortfolioTreeHistoryRequest;
import com.opengamma.master.position.PortfolioTreeHistoryResult;
import com.opengamma.masterdb.position.QueryPortfolioTreeDbPositionMasterWorker;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPortfolioTreeDbPositionMasterWorker.
 */
public class QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreeHistoricTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreeHistoricTest.class);

  private QueryPortfolioTreeDbPositionMasterWorker _worker;

  public QueryPortfolioTreeDbPositionMasterWorkerSearchPortfolioTreeHistoricTest(String databaseType, String databaseVersion) {
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
  public void test_searchPortfolioTreeHistoric_documents_noInstants_nodeTree_depthZero() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);  // new version
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);  // old version
    
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
    assertEquals(_version2Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version2Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "1"), doc0.getPortfolio().getRootNode().getUniqueIdentifier());
    
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getPortfolioId());
    assertEquals(_version1Instant, doc1.getVersionFromInstant());
    assertEquals(_version2Instant, doc1.getVersionToInstant());
    assertEquals(_version1Instant, doc1.getCorrectionFromInstant());
    assertEquals(null, doc1.getCorrectionToInstant());
    
    ManageablePortfolioNode rootNode = doc1.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "211", "0"), rootNode.getUniqueIdentifier());
    assertEquals(0, rootNode.getChildNodes().size());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_documents_noInstants_nodeTree_depthOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "101");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setDepth(1);
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getPortfolioId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    
    ManageablePortfolioNode rootNode = doc0.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode.getUniqueIdentifier());
    assertEquals(1, rootNode.getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "0"), rootNode.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().size());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_documents_noInstants_nodeTree_maxDepth() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "101");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setDepth(-1);
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    
    assertEquals(UniqueIdentifier.of("DbPos", "101", "0"), doc0.getPortfolioId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    
    ManageablePortfolioNode rootNode = doc0.getPortfolio().getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode.getUniqueIdentifier());
    assertEquals(1, rootNode.getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "0"), rootNode.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "113", "0"), rootNode.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPortfolioTreeHistoric_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPortfolioTreeHistoric_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc0.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPortfolioTreeHistoric_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getPortfolioId());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getPortfolioId());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPortfolioTreeHistoric_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc0.getPortfolioId());
  }

  @Test
  public void test_searchPortfolioTreeHistoric_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeHistoryRequest request = new PortfolioTreeHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PortfolioTreeHistoryResult test = _worker.historyPortfolioTree(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PortfolioTreeDocument doc0 = test.getDocuments().get(0);
    PortfolioTreeDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "201", "1"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), doc1.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
