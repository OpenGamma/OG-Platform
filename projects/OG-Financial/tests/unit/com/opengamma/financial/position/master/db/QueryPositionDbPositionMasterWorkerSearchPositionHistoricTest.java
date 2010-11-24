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
import com.opengamma.financial.position.master.PositionHistoryRequest;
import com.opengamma.financial.position.master.PositionHistoryResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerSearchPositionHistoricTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerSearchPositionHistoricTest.class);

  private DbPositionMasterWorker _worker;

  public QueryPositionDbPositionMasterWorkerSearchPositionHistoricTest(String databaseType, String databaseVersion) {
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
  public void test_searchPositionHistoric_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);  // new version
    PositionDocument doc1 = test.getDocuments().get(1);  // old version
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), doc0.getParentNodeId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(doc0.getVersionFromInstant(), doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    ManageablePosition position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(222.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(1, secKey0.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey0.getIdentifiers().iterator().next());
    Set<ManageableTrade> trades = position0.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(222.987), _version2Instant.minusSeconds(100), Identifier.of("CPARTY", "C222"));
    trade.setPositionId(position0.getUniqueIdentifier());
    trade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "407", "1"));
    assertTrue(trades.contains(trade));
    
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc1.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "201"), doc1.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "211"), doc1.getParentNodeId());
    assertNotNull(doc1.getVersionFromInstant());
    assertEquals(doc0.getVersionFromInstant(), doc1.getVersionToInstant());
    assertEquals(doc1.getVersionFromInstant(), doc1.getCorrectionFromInstant());
    assertEquals(null, doc1.getCorrectionToInstant());
    ManageablePosition position1 = doc1.getPosition();
    assertNotNull(position1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), position1.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(221.987), position1.getQuantity());
    IdentifierBundle secKey1 = position1.getSecurityKey();
    assertNotNull(secKey1);
    assertEquals(1, secKey1.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey1.getIdentifiers().iterator().next());
    trades = position1.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    trade = new ManageableTrade(BigDecimal.valueOf(221.987), _version1Instant.minusSeconds(100), Identifier.of("CPARTY", "C221"));
    trade.setPositionId(position1.getUniqueIdentifier());
    trade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "407", "0"));
    assertTrue(trades.contains(trade));
  }

  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecurities() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "121");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);  // new version
    
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc0.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc0.getParentNodeId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(doc0.getVersionFromInstant(), doc0.getCorrectionFromInstant());
    assertEquals(null, doc0.getCorrectionToInstant());
    ManageablePosition position0 = doc0.getPosition();
    assertNotNull(position0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), position0.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), position0.getQuantity());
    IdentifierBundle secKey0 = position0.getSecurityKey();
    assertNotNull(secKey0);
    assertEquals(2, secKey0.size());
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertTrue(secKey0.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Set<ManageableTrade> trades = position0.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    ManageableTrade trade = trades.iterator().next();
    assertNotNull(trade);
    assertEquals(Identifier.of("CPARTY", "C101"), trade.getCounterpartyId());
    assertEquals(BigDecimal.valueOf(121.987), trade.getQuantity());
    assertEquals(_version1Instant.minusSeconds(121), trade.getTradeInstant());
  }
  
  @Test
  public void test_searchPositionHistoric_documentCountWhenMultipleSecuritiesAndMultipleTrades() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "123");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc = test.getDocuments().get(0);  // new version
    
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), doc.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), doc.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "112"), doc.getParentNodeId());
    assertEquals(_version1Instant, doc.getVersionFromInstant());
    assertEquals(null, doc.getVersionToInstant());
    assertEquals(_version1Instant, doc.getCorrectionFromInstant());
    assertEquals(null, doc.getCorrectionToInstant());
    ManageablePosition position = doc.getPosition();
    assertNotNull(position);
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), position.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(123.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "ORCL135")));
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "ORCL134")));
    Set<ManageableTrade> trades = position.getTrades();
    assertNotNull(trades);
    assertEquals(3, trades.size());
    
    ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(100.987), _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C104"));
    trade.setPositionId(position.getUniqueIdentifier());
    trade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "404", "0"));
    assertTrue(trades.contains(trade));
    
    trade = new ManageableTrade(BigDecimal.valueOf(200.987), _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C105"));
    trade.setPositionId(position.getUniqueIdentifier());
    trade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "405", "0"));
    assertTrue(trades.contains(trade));
    
    trade = new ManageableTrade(BigDecimal.valueOf(300.987), _version1Instant.minusSeconds(123), Identifier.of("CPARTY", "C106"));
    trade.setPositionId(position.getUniqueIdentifier());
    trade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "406", "0"));
    assertTrue(trades.contains(trade));
    
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc1.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
  }

  @Test
  public void test_searchPositionHistoric_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc0.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc1.getPositionId());
  }

  @Test
  public void test_searchPositionHistoric_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc1.getPositionId());
  }

  @Test
  public void test_searchPositionHistoric_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchPositionHistoric_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc0.getPositionId());
  }

  @Test
  public void test_searchPositionHistoric_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "221");
    PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    PositionHistoryResult test = _worker.historyPosition(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    PositionDocument doc0 = test.getDocuments().get(0);
    PositionDocument doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), doc0.getPositionId());
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), doc1.getPositionId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
