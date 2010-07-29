/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests Search Historic Positions DbPositionMasterWorker.
 */
public class SearchHistoricPositionsDbPositionMasterWorkerTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(SearchHistoricPositionsDbPositionMasterWorkerTest.class);

  private GetPositionDbPositionMasterWorker _worker;

  public SearchHistoricPositionsDbPositionMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new GetPositionDbPositionMasterWorker();
    _worker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricPositions_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);  // new version
    PositionDocument doc1 = test.getDocuments().get(1);  // old version
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), doc0.getParentNodeId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(doc0.getVersionFromInstant(), doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    Position position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(222.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(1, secKey0.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey0.getIdentifiers().iterator().next());
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc1.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), doc1.getParentNodeId());
    assertNotNull(doc1.getVersionFromInstant());
    assertEquals(doc0.getVersionFromInstant(), doc1.getVersionToInstant());
    assertEquals(doc1.getVersionFromInstant(), doc1.getCorrectionFromInstant());
    assertEquals(null, doc1.getCorrectionToInstant());
    Position position1 = doc1.getPosition();
    assertNotNull(position1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), position1.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(221.987), position1.getQuantity());
    IdentifierBundle secKey1 = position1.getSecurityKey();
    assertNotNull(secKey1);
    assertEquals(1, secKey1.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey1.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricPositions_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc1.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricPositions_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
  }

  @Test
  public void test_searchHistoricPositions_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc0.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricPositions_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc1.getPositionId());
  }

  @Test
  public void test_searchHistoricPositions_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc1.getPositionId());
  }

  @Test
  public void test_searchHistoricPositions_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHistoricPositions_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchHistoricPositions_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc0.getPositionId());
  }

  @Test
  public void test_searchHistoricPositions_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PositionSearchHistoricResult test = _worker.searchPositionHistoric(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc1.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
