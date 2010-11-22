/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests QueryFullDbPositionMasterWorker.
 */
public class QueryFullDbPositionMasterWorkerGetFullPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorkerGetFullPositionTest.class);

  private QueryFullDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public QueryFullDbPositionMasterWorkerGetFullPositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryFullDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPositionDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getFullPosition_nullUID() {
    _worker.getFullPosition(null);
  }

  @Test
  public void test_getFullPosition__notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    assertNull(test);
  }

  @Test
  public void test_getFullPosition_oneSecurityKey_oneTrade() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "120");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(120.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(1, testSecKey.size());
    assertEquals(Identifier.of("TICKER", "T130"), testSecKey.getIdentifiers().iterator().next());
    
    Collection<Trade> trades = test.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    Trade trade = trades.iterator().next();
    assertNotNull(trade);
    assertEquals(Identifier.of("CPARTY", "C100"), trade.getCounterparty().getIdentifier());
    assertEquals(BigDecimal.valueOf(120.987), trade.getQuantity());
    assertEquals(testSecKey, trade.getSecurityKey());
    assertNotNull(trade.getTradeInstant());
  }
  
  @Test
  public void test_getFullPosition_oneSecurityKey_twoTrades() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(1, testSecKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey.getIdentifiers().iterator().next());
    
    Collection<Trade> trades = test.getTrades();
    assertNotNull(trades);
    assertEquals(2, trades.size());
    
    assertTrue(trades.contains(new TradeImpl(test, BigDecimal.valueOf(100.987), new CounterpartyImpl(Identifier.of("CPARTY", "JMP")), _version1Instant.minusSeconds(122))));
    assertTrue(trades.contains(new TradeImpl(test, BigDecimal.valueOf(22.987), new CounterpartyImpl(Identifier.of("CPARTY", "CISC")), _version1Instant.minusSeconds(122))));
    
  }

  @Test
  public void test_getPosition_twoSecurityKeys_oneTrade() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(2, testSecKey.size());
    assertEquals(true, testSecKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Collection<Trade> trades = test.getTrades();
    assertNotNull(trades);
    assertEquals(1, trades.size());
    Trade trade = trades.iterator().next();
    assertNotNull(trade);
    assertEquals(Identifier.of("CPARTY", "C101"), trade.getCounterparty().getIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), trade.getQuantity());
    assertEquals(testSecKey, trade.getSecurityKey());
    assertEquals(_version1Instant.minusSeconds(121), trade.getTradeInstant());
  }
  
  @Test
  public void test_getPosition_twoSecurityKeys_twoTrades() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "123");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(123.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(2, testSecKey.size());
    assertEquals(true, testSecKey.getIdentifiers().contains(Identifier.of("TICKER", "ORCL134")));
    assertEquals(true, testSecKey.getIdentifiers().contains(Identifier.of("NASDAQ", "ORCL135")));
    
    Collection<Trade> trades = test.getTrades();
    assertNotNull(trades);
    assertEquals(3, trades.size());
    
    assertTrue(trades.contains(new TradeImpl(test, BigDecimal.valueOf(100.987), new CounterpartyImpl(Identifier.of("CPARTY", "C104")), _version1Instant.minusSeconds(123))));
    assertTrue(trades.contains(new TradeImpl(test, BigDecimal.valueOf(200.987), new CounterpartyImpl(Identifier.of("CPARTY", "C105")), _version1Instant.minusSeconds(123))));
    assertTrue(trades.contains(new TradeImpl(test, BigDecimal.valueOf(300.987), new CounterpartyImpl(Identifier.of("CPARTY", "C106")), _version1Instant.minusSeconds(123))));
    
  }

  @Test
  public void test_getPosition_versioned_noInstants_chooseLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "1"), test.getUniqueIdentifier());
  }

  @Test
  public void test_getPosition_versioned_notLatest_instant() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221");
    FullPositionGetRequest request = new FullPositionGetRequest(uid);
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    request.setCorrectedToInstant(_version2Instant.plusSeconds(5));
    Position test = _worker.getFullPosition(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "221", "0"), test.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(221.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(1, testSecKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), testSecKey.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
