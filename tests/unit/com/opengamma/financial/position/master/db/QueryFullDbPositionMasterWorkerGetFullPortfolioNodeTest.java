/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests QueryFullDbPositionMasterWorker.
 */
public class QueryFullDbPositionMasterWorkerGetFullPortfolioNodeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorkerGetFullPortfolioNodeTest.class);

  private QueryFullDbPositionMasterWorker _worker;
  private QueryPositionDbPositionMasterWorker _queryWorker;

  public QueryFullDbPositionMasterWorkerGetFullPortfolioNodeTest(String databaseType, String databaseVersion) {
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
  public void test_getFullPortfolioNode_nullUID() {
    _worker.getFullPortfolioNode(null);
  }

  @Test
  public void test_getFullPortfolioNode_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    assertNull(test);
  }

  @Test
  public void test_getFullPortfolioNode_111() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "111");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "111", "111"), test.getUniqueIdentifier());
    assertEquals("TestNode111", test.getName());
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.getChildNodes().size());
    
    PortfolioNode testChild112 = test.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "112", "112"), testChild112.getUniqueIdentifier());
    assertEquals("TestNode112", testChild112.getName());
    assertEquals(2, testChild112.getPositions().size());
    assertEquals(1, testChild112.getChildNodes().size());
    
    PortfolioNode testChild113 = testChild112.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113", "113"), testChild113.getUniqueIdentifier());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    Position testPos121 = testChild112.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Position testPos122 = testChild112.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), testPos122.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), testPos122.getQuantity());
    IdentifierBundle testSecKey122 = testPos122.getSecurityKey();
    assertNotNull(testSecKey122);
    assertEquals(1, testSecKey122.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey122.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getFullPortfolioNode_112() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "112");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "112"), test.getUniqueIdentifier());
    assertEquals("TestNode112", test.getName());
    assertEquals(2, test.getPositions().size());
    assertEquals(1, test.getChildNodes().size());
    
    PortfolioNode testChild113 = test.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113", "113"), testChild113.getUniqueIdentifier());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    Position testPos121 = test.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "121"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Position testPos122 = test.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "122"), testPos122.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), testPos122.getQuantity());
    IdentifierBundle testSecKey122 = testPos122.getSecurityKey();
    assertNotNull(testSecKey122);
    assertEquals(1, testSecKey122.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey122.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getFullPortfolioNode_113() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "113");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "113", "113"), test.getUniqueIdentifier());
    assertEquals("TestNode113", test.getName());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.getChildNodes().size());
  }

  @Test
  public void test_getFullPortfolioNode_noInstants_chooseLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "211");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "211", "212"), test.getUniqueIdentifier());
  }

  @Test
  public void test_getFullPortfolioNode_notLatest_instant() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "211");
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    request.setCorrectedToInstant(_version2Instant.plusSeconds(5));
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "211", "211"), test.getUniqueIdentifier());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
