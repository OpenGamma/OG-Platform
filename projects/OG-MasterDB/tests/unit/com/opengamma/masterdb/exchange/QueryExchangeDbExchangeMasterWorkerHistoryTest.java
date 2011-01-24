/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryExchangeDbExchangeMasterWorker.
 */
public class QueryExchangeDbExchangeMasterWorkerHistoryTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorkerHistoryTest.class);

  public QueryExchangeDbExchangeMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleExchanges() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "102");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    ExchangeHistoryResult test = _exgMaster.history(request);
    
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
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
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
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbExg", "201");
    ExchangeHistoryRequest request = new ExchangeHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ExchangeHistoryResult test = _exgMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_exgMaster.getClass().getSimpleName() + "[DbExg]", _exgMaster.toString());
  }

}
