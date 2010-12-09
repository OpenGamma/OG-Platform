/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Trade;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.FullTradeGetRequest;

/**
 * Tests QueryFullDbPositionMasterWorker.
 */
public class QueryFullDbPositionMasterWorkerGetFullTradeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorkerGetFullTradeTest.class);

  private QueryFullDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public QueryFullDbPositionMasterWorkerGetFullTradeTest(String databaseType, String databaseVersion) {
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
  public void test_getFullTrade_nullUID() {
    _worker.getFullTrade(null);
  }

  @Test
  public void test_getFullTrade__notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    FullTradeGetRequest request = new FullTradeGetRequest(uid);
    Trade test = _worker.getFullTrade(request);
    assertNull(test);
  }

  @Test
  public void test_getFullTrade_singleSecurity() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "403", "0");
    FullTradeGetRequest request = new FullTradeGetRequest(uid);
    Trade test = _worker.getFullTrade(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "403", "0"), test.getUniqueIdentifier());
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), test.getPositionId());
    assertEquals(BigDecimal.valueOf(22.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(1, testSecKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey.getIdentifiers().iterator().next());
    
    assertEquals(_now.toLocalDate(), test.getTradeDate());
    assertEquals(_now.toOffsetTime().minusSeconds(222), test.getTradeTime());
    
  }
  
  @Test
  public void test_getFullTrade_multipleSecurity() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "404", "0");
    FullTradeGetRequest request = new FullTradeGetRequest(uid);
    Trade test = _worker.getFullTrade(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "404", "0"), test.getUniqueIdentifier());
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), test.getPositionId());
    assertEquals(BigDecimal.valueOf(100.987), test.getQuantity());
    IdentifierBundle testSecKey = test.getSecurityKey();
    assertNotNull(testSecKey);
    assertEquals(2, testSecKey.size());
    assertTrue(testSecKey.contains(Identifier.of("TICKER", "ORCL134")));
    assertTrue(testSecKey.contains(Identifier.of("NASDAQ", "ORCL135")));
    
    assertEquals(_now.toLocalDate(), test.getTradeDate());
    assertEquals(_now.toOffsetTime().minusSeconds(223), test.getTradeTime());
    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void test_getTrade_noVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221");
    FullTradeGetRequest request = new FullTradeGetRequest(uid);
    _worker.getFullTrade(request);
    
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
