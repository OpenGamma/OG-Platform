/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.masterdb.position.QueryPortfolioTreeDbPositionMasterWorker;

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
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio101", portfolio.getName());
    
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), rootNode.getUniqueIdentifier());
    assertEquals("TestNode111", rootNode.getName());
    assertEquals(1, rootNode.getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "112", "0"), rootNode.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode112", rootNode.getChildNodes().get(0).getName());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    
    assertEquals(UniqueIdentifier.of("DbPos", "113", "0"), rootNode.getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
    assertEquals("TestNode113", rootNode.getChildNodes().get(0).getChildNodes().get(0).getName());
    assertEquals(0, rootNode.getChildNodes().get(0).getChildNodes().get(0).getChildNodes().size());
  }

  @Test
  public void test_getPortfolioTree_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "0");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio201", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "0"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode211", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
  }

  @Test
  public void test_getPortfolioTree_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "1");
    PortfolioTreeDocument test = _worker.getPortfolioTree(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio202", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "1"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
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
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "1");
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio202", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "211", "1"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode212", portfolio.getRootNode().getName());
    assertEquals(0, portfolio.getRootNode().getChildNodes().size());
  }

  @Test
  public void test_getPortfolioTree_unversioned_nodesLoaded() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "101");
    PortfolioTreeDocument test = _worker.getPortfolioTree(oid);
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101", "0");
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio portfolio = test.getPortfolio();
    assertEquals(uid, portfolio.getUniqueIdentifier());
    assertEquals("TestPortfolio101", portfolio.getName());
    assertEquals(UniqueIdentifier.of("DbPos", "111", "0"), portfolio.getRootNode().getUniqueIdentifier());
    assertEquals("TestNode111", portfolio.getRootNode().getName());
    assertEquals(1, portfolio.getRootNode().getChildNodes().size());
    assertEquals(1, portfolio.getRootNode().getChildNodes().get(0).getChildNodes().size());
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_getPortfolioTree_fullId_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "201", "0-0");
    _worker.getPortfolioTree(uid);
  }

  @Test
  public void test_getPortfolioTree_fullId_firstVersion() {
    UniqueIdentifier fullId = UniqueIdentifier.of("DbPos", "201", Long.toHexString(_version1Instant.toEpochMillisLong()) + "-0");
    PortfolioTreeDocument test = _worker.getPortfolioTree(fullId);
    
    assertEquals(UniqueIdentifier.of("DbPos", "201", "0"), test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
