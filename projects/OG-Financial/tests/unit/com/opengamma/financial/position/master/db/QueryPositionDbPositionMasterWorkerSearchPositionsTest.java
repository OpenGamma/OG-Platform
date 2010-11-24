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
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.ManageableTrade;
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

  private DbPositionMasterWorker _worker;

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
    
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    ManageablePosition position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(100.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(1, secKey0.size());
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("TICKER", "S100")));
    
    Set<ManageableTrade> trades = position0.getTrades();
    assertNotNull(trades);
    assertTrue(trades.isEmpty());
//    ManageableTrade trade0 = trades.iterator().next();
//    assertNotNull(trade0);
//    assertEquals(Identifier.of("CPARTY", "C100"), trade0.getCounterpartyId());
//    assertEquals(BigDecimal.valueOf(120.987), trade0.getQuantity());
//    assertEquals(_version1Instant.minusSeconds(120), trade0.getTradeInstant());
    
  }
  
  @Test
  public void test_searchPositions_noTrades() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setParentNodeId(UniqueIdentifier.of("DbPos", "112"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(5, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertEquals(_version1Instant, doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(_version1Instant, doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    ManageablePosition position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(100.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(1, secKey0.size());
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("TICKER", "S100")));
    
    Set<ManageableTrade> trades0 = position0.getTrades();
    assertNotNull(trades0);
    assertTrue(trades0.isEmpty());
  }
  
  

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_portfolioId_101() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "101"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(5, test.getPaging().getTotalItems());
    assertEquals(5, test.getDocuments().size());
    
    PositionDocument doc = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    ManageablePosition position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(100.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "S100")));
    Set<ManageableTrade> trades = position.getTrades();
    assertNotNull(trades);
    assertTrue(trades.isEmpty());
    
    doc = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(120.987), position.getQuantity());
    secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "T130")));
    trades = position.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    ManageableTrade trade = trades.iterator().next();
    assertNotNull(trade);
    assertEquals(Identifier.of("CPARTY", "C100"), trade.getCounterpartyId());
    assertEquals(BigDecimal.valueOf(120.987), trade.getQuantity());
    assertEquals(_version1Instant.minusSeconds(120), trade.getTradeInstant());
    
    doc = test.getDocuments().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), position.getQuantity());
    secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    trades = position.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    trade = trades.iterator().next();
    assertNotNull(trade);
    assertEquals(Identifier.of("CPARTY", "C101"), trade.getCounterpartyId());
    assertEquals(BigDecimal.valueOf(121.987), trade.getQuantity());
    assertEquals(_version1Instant.minusSeconds(121), trade.getTradeInstant());
    
    //test position with 1 security and 2 trades
    doc = test.getDocuments().get(3);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), position.getQuantity());
    secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "ORCL")));
    trades = position.getTrades();
    assertNotNull(trades);
    assertEquals(2, trades.size());
    assertTrue(trades.contains(new ManageableTrade(UniqueIdentifier.of("DbPos", "402", "0"), position.getUniqueIdentifier(), BigDecimal.valueOf(100.987), 
        _version1Instant.minusSeconds(122), Identifier.of("CPARTY", "JMP"))));
    assertTrue(trades.contains(new ManageableTrade(UniqueIdentifier.of("DbPos", "403", "0"), position.getUniqueIdentifier(), BigDecimal.valueOf(22.987), 
        _version1Instant.minusSeconds(122), Identifier.of("CPARTY", "CISC"))));
    
    //test position with 2 securities and 3 trades
    doc = test.getDocuments().get(4);
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(123.987), position.getQuantity());
    secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "ORCL135")));
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "ORCL134")));
    trades = position.getTrades();
    assertNotNull(trades);
    assertEquals(3, trades.size());
    assertTrue(trades.contains(new ManageableTrade(UniqueIdentifier.of("DbPos", "404", "0"), position.getUniqueIdentifier(), BigDecimal.valueOf(100.987), 
        _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C104"))));
    assertTrue(trades.contains(new ManageableTrade(UniqueIdentifier.of("DbPos", "405", "0"), position.getUniqueIdentifier(), BigDecimal.valueOf(200.987), 
        _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C105"))));
    assertTrue(trades.contains(new ManageableTrade(UniqueIdentifier.of("DbPos", "406", "0"), position.getUniqueIdentifier(), BigDecimal.valueOf(300.987), 
        _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C106"))));
    
  }

  @Test
  public void test_searchPositions_portfolioId_201() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "201"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc0.getPortfolioId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_parentNodeId_112() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPortfolioId(UniqueIdentifier.of("DbPos", "101"));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(5, test.getPaging().getTotalItems());
    
    assertEquals(5, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc1.getParentNodeId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc2.getParentNodeId());
    
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc3.getParentNodeId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc4.getParentNodeId());
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
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
  }

  @Test
  public void test_searchPositions_pageTwo() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc1.getPositionId());
  }
  
  @Test
  public void test_searchPositions_pageThree() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(3, 2));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(5, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc1.getPositionId());
  }
  

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositions_minQuantity_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(50));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(6, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    PositionDocument doc5 = test.getDocuments().get(5);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc5.getPositionId());
  }

  @Test
  public void test_searchPositions_minQuantity_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(150));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
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
    
    assertEquals(5, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
  }

  @Test
  public void test_searchPositions_maxQuantity_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(450));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(6, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    PositionDocument doc5 = test.getDocuments().get(5);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc5.getPositionId());
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
    
    assertEquals(6, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    PositionDocument doc5 = test.getDocuments().get(5);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc5.getPositionId());  // old version
  }

  @Test
  public void test_searchPositions_versionAsOf_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    PositionSearchResult test = _worker.searchPositions(request);
    
    assertEquals(6, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    PositionDocument doc2 = test.getDocuments().get(2);
    PositionDocument doc3 = test.getDocuments().get(3);
    PositionDocument doc4 = test.getDocuments().get(4);
    PositionDocument doc5 = test.getDocuments().get(5);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc2.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), doc3.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc4.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc5.getPositionId());  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
