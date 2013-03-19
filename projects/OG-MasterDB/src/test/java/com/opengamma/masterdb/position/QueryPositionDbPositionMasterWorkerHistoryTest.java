/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPositionDbPositionMasterWorkerHistoryTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_documents() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecurities() {
    ObjectId oid = ObjectId.of("DbPos", "121");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecuritiesAndMultipleTrades() {
    ObjectId oid = ObjectId.of("DbPos", "123");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert123(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants_pageOne() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.ofPage(1, 1));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_noInstants_pageTwo() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.ofPage(2, 1));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItemOneBased());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_versionsFrom_preFirst() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  @Test
  public void test_searchPositionHistoric_versionsFrom_firstToSecond() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  @Test
  public void test_searchPositionHistoric_versionsFrom_postSecond() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_versionsTo_preFirst() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_firstToSecond() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_postSecond() {
    ObjectId oid = ObjectId.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

}
