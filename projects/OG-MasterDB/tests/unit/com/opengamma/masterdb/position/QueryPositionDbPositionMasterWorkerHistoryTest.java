/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerHistoryTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerHistoryTest.class);

  public QueryPositionDbPositionMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecurities() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "121");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecuritiesAndMultipleTrades() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "123");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert123(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
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
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PositionHistoryResult test = _posMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
