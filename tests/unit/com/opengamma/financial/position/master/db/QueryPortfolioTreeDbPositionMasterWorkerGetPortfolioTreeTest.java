/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests QueryPortfolioTreeDbPositionMasterWorker.
 */
public class QueryPortfolioTreeDbPositionMasterWorkerGetPortfolioTreeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPortfolioTreeDbPositionMasterWorkerGetPortfolioTreeTest.class);

  private QueryPortfolioTreeDbPositionMasterWorker _worker;

  public QueryPortfolioTreeDbPositionMasterWorkerGetPortfolioTreeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryPortfolioTreeDbPositionMasterWorker();
    _worker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getPortfolioTree_nullUID() {
    _worker.getPortfolioTree(null);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_getPortfolioTree_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _worker.getPortfolioTree(uid);
  }

  @Test
  public void test_getPortfolioTree_versioned() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "101");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPortfolioId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    Portfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio101", portfolio.getName());
    
    PortfolioNode rootNode = portfolio.getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "111"), rootNode.getUniqueIdentifier());
    assertEquals("TestNode111", rootNode.getName());
    assertEquals(1, rootNode.getChildNodes().size());
    assertEquals(0, rootNode.getPositions().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "112"), rootNode.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode112", rootNode.getChildNodes().get(0).getName());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    assertEquals(0, rootNode.getChildNodes().get(0).getPositions().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "113", "113"), rootNode.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode113", rootNode.getChildNodes().get(0).getChildNodes().get(0).getName());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().get(0).getPositions().size());
  }

  @Test
  public void test_getPortfolioTree_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "201");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPortfolioId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    Portfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio201", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "211"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode211", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
    assertEquals(0, portfolio.getRootNode().getPositions().size());
  }

  @Test
  public void test_getPortfolioTree_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "202");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertNotNull(test);
    assertEquals(uid, test.getPortfolioId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    Portfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio202", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "212"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
    assertEquals(0, portfolio.getRootNode().getPositions().size());
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getPortfolioTree_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _worker.getPortfolioTree(uid);
  }

  @Test
  public void test_getPortfolioTree_unversioned_latest() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "201");
    PortfolioTreeDocument test = _worker.getPortfolioTree(oid);
    
    assertNotNull(test);
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "202");
    assertEquals(uid, test.getPortfolioId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    Portfolio portfolio = test.getPortfolio();
    assertNotNull(portfolio);
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio202", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "212"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
    assertEquals(0, portfolio.getRootNode().getPositions().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
