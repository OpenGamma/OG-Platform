/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPortfolioDbPortfolioMasterWorkerHistoryTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPortfolioDbPortfolioMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents_noInstants_node_depthZero() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documents_noInstants_node_depthOne() {
    UniqueId oid = UniqueId.of("DbPrt", "101");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setDepth(1);
    PortfolioHistoryResult test = _prtMaster.history(request);
    assert101(test.getDocuments().get(0), 1);
  }

  @Test
  public void test_history_documents_noInstants_node_maxDepth() {
    UniqueId oid = UniqueId.of("DbPrt", "101");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setDepth(-1);
    PortfolioHistoryResult test = _prtMaster.history(request);
    assert101(test.getDocuments().get(0), 999);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PagingRequest pr = PagingRequest.ofPage(1, 1);
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setPagingRequest(pr);
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PagingRequest pr = PagingRequest.ofPage(2, 1);
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setPagingRequest(pr);
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueId oid = UniqueId.of("DbPrt", "201");
    PortfolioHistoryRequest request = new PortfolioHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PortfolioHistoryResult test = _prtMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
