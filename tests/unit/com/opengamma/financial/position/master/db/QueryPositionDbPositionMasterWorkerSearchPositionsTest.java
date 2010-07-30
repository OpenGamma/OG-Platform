/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerSearchPositionsTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerSearchPositionsTest.class);

  private QueryPositionDbPositionMasterWorker _worker;

  public QueryPositionDbPositionMasterWorkerSearchPositionsTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPositionDbPositionMasterWorker();
    _worker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_documents() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(_totalPositions, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    Position position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(2, secKey0.size());
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_portfolioId_101() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "101"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc1.getPortfolioId());
  }

  @Test
  public void test_searchPositions_portfolioId_201() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "201"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc0.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_parentNodeId_112() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "101"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc1.getParentNodeId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_pageOne() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 2));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
  }

  @Test
  public void test_searchPositions_pageTwo() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_minQuantity_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(50));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc2.getPositionId());
  }

  @Test
  public void test_searchPositions_minQuantity_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(150));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc0.getPositionId());
  }

  @Test
  public void test_searchPositions_minQuantity_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(450));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_maxQuantity_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(50));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPositions_maxQuantity_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(150));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
  }

  @Test
  public void test_searchPositions_maxQuantity_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(450));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc2.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_versionAsOf_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPositions_versionAsOf_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "221"), doc2.getPositionId());  // old version
  }

  @Test
  public void test_searchPositions_versionAsOf_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "222"), doc2.getPositionId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
