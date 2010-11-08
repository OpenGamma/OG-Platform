/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.world.exchange.master.ExchangeHistoryRequest;
import com.opengamma.financial.world.exchange.master.ExchangeHistoryResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryExchangeDbExchangeMasterWorker.
 */
public class QueryExchangeDbExchangeMasterWorkerHistoryTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorkerHistoryTest.class);

  private DbExchangeMasterWorker _worker;

  public QueryExchangeDbExchangeMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
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
  public void test_history_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleExchanges() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "102");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ExchangeHistoryResult test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
