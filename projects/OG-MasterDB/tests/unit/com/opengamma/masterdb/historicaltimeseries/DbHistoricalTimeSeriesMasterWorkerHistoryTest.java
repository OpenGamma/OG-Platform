/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.test.DBTest;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerHistoryTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricalTimeSeriesHistoric_documents() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricalTimeSeriesHistoric_noInstants() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricalTimeSeriesHistoric_noInstants_pageOne() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.of(1, 1));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_searchHistoricalTimeSeriesHistoric_noInstants_pageTwo() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.of(2, 1));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
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
  public void test_searchHistoricalTimeSeriesHistoric_versionsFrom_preFirst() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_searchHistoricalTimeSeriesHistoric_versionsFrom_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_searchHistoricalTimeSeriesHistoric_versionsFrom_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricalTimeSeriesHistoric_versionsTo_preFirst() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchHistoricalTimeSeriesHistoric_versionsTo_firstToSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_searchHistoricalTimeSeriesHistoric_versionsTo_postSecond() {
    ObjectIdentifier oid = ObjectIdentifier.of("DbHts", "201");
    HistoricalTimeSeriesHistoryRequest request = new HistoricalTimeSeriesHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    HistoricalTimeSeriesHistoryResult test = _htsMaster.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
