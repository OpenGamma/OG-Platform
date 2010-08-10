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
import java.util.TimeZone;

import javax.time.Instant;

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
  private DbPositionMasterWorker _queryWorker;

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
    
    assertEquals(UniqueIdentifier.of("DbPos", "111"), test.getUniqueIdentifier().toLatest());
    assertEquals("TestNode111", test.getName());
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.getChildNodes().size());
    
    PortfolioNode testChild112 = test.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "112"), testChild112.getUniqueIdentifier().toLatest());
    assertEquals("TestNode112", testChild112.getName());
    assertEquals(2, testChild112.getPositions().size());
    assertEquals(1, testChild112.getChildNodes().size());
    
    PortfolioNode testChild113 = testChild112.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113"), testChild113.getUniqueIdentifier().toLatest());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    Position testPos121 = testChild112.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Position testPos122 = testChild112.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), testPos122.getUniqueIdentifier());
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
    
    assertEquals(UniqueIdentifier.of("DbPos", "112"), test.getUniqueIdentifier().toLatest());
    assertEquals("TestNode112", test.getName());
    assertEquals(2, test.getPositions().size());
    assertEquals(1, test.getChildNodes().size());
    
    PortfolioNode testChild113 = test.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113"), testChild113.getUniqueIdentifier().toLatest());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    Position testPos121 = test.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Position testPos122 = test.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), testPos122.getUniqueIdentifier());
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
    
    assertEquals(UniqueIdentifier.of("DbPos", "113"), test.getUniqueIdentifier().toLatest());
    assertEquals("TestNode113", test.getName());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.getChildNodes().size());
  }

  @Test
  public void test_getFullPortfolioNode_111_sameUidDifferentInstant() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "111");
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid, now, now);
    PortfolioNode testNow = _worker.getFullPortfolioNode(request);
    
    Instant later = now.plusSeconds(100);
    FullPortfolioNodeGetRequest requestLater = new FullPortfolioNodeGetRequest(uid, later, later);
    PortfolioNode testLater = _worker.getFullPortfolioNode(requestLater);
    
    assertEquals(testLater.getUniqueIdentifier(), testNow.getUniqueIdentifier());
    assertEquals(testLater.getChildNodes().get(0).getUniqueIdentifier(), testNow.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(testLater.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier(), testNow.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
  }

  @Test
  public void test_getFullPortfolioNode_111_112_sameUidDifferentInstant() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "111"), now, now);
    PortfolioNode test111Now = _worker.getFullPortfolioNode(request);
    
    Instant later = now.plusSeconds(100);
    FullPortfolioNodeGetRequest requestLater = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "112"), later, later);
    PortfolioNode testLater = _worker.getFullPortfolioNode(requestLater);
    
    assertEquals(testLater.getUniqueIdentifier(), test111Now.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(testLater.getChildNodes().get(0).getUniqueIdentifier(), test111Now.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
  }

  @Test
  public void test_getFullPortfolioNode_latest_notLatest() {
    // latest
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioNodeGetRequest requestLatest = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "211"), now, now);
    PortfolioNode testLatest = _worker.getFullPortfolioNode(requestLatest);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), testLatest.getUniqueIdentifier().toLatest());
    
    // earlier
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioNodeGetRequest requestEarlier = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "211"), earlier, earlier);
    PortfolioNode testEarlier = _worker.getFullPortfolioNode(requestEarlier);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), testEarlier.getUniqueIdentifier().toLatest());
    
    // ensure earlier is actually earlier
    assertTrue(testEarlier.getUniqueIdentifier().getVersion().compareTo(testLatest.getUniqueIdentifier().getVersion()) < 0);
  }

  @Test
  public void test_getFullPortfolioNode_byFullPortfolioNodeUid_latest() {
    // not latest version
    Instant later = _version2Instant.plusSeconds(5);
    FullPortfolioNodeGetRequest requestBase = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "211"), later, later);
    PortfolioNode testBase = _worker.getFullPortfolioNode(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version2Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  @Test
  public void test_getFullPortfolioNode_byFullPortfolioNodeUid_notLatest() {
    // not latest version
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioNodeGetRequest requestBase = new FullPortfolioNodeGetRequest(UniqueIdentifier.of("DbPos", "211"), earlier, earlier);
    PortfolioNode testBase = _worker.getFullPortfolioNode(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    PortfolioNode test = _worker.getFullPortfolioNode(request);
    assertEquals(UniqueIdentifier.of("DbPos", "211"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version1Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
