/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.masterdb.position.ModifyPortfolioTreeDbPositionMasterWorker;
import com.opengamma.masterdb.position.QueryPortfolioTreeDbPositionMasterWorker;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPortfolioTreeDbPositionMasterWorkerAddPortfolioTreeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioTreeDbPositionMasterWorkerAddPortfolioTreeTest.class);

  private ModifyPortfolioTreeDbPositionMasterWorker _worker;
  private QueryPortfolioTreeDbPositionMasterWorker _queryWorker;

  public ModifyPortfolioTreeDbPositionMasterWorkerAddPortfolioTreeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyPortfolioTreeDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPortfolioTreeDbPositionMasterWorker();
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
  public void test_addPortfolioTree_nullDocument() {
    _worker.addPortfolioTree(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_addPortfolioTree_noPortfolio() {
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    _worker.addPortfolioTree(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_addPortfolioTree_noRootNode() {
    ManageablePortfolio mockPortfolio = mock(ManageablePortfolio.class);
    when(mockPortfolio.getName()).thenReturn("Test");
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(mockPortfolio);
    _worker.addPortfolioTree(doc);
  }

  @Test
  public void test_addPortfolioTree_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(portfolio);
    PortfolioTreeDocument test = _worker.addPortfolioTree(doc);
    
    UniqueIdentifier uid = test.getPortfolioId();
    assertNotNull(uid);
    assertEquals("DbPos", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) > 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePortfolio testPortfolio = test.getPortfolio();
    assertEquals(uid, testPortfolio.getUniqueIdentifier());
    assertEquals("Test", testPortfolio.getName());
    ManageablePortfolioNode testRootNode = testPortfolio.getRootNode();
    assertEquals("Root", testRootNode.getName());
    assertEquals(1, testRootNode.getChildNodes().size());
    ManageablePortfolioNode testChildNode = testRootNode.getChildNodes().get(0);
    assertEquals("Child", testChildNode.getName());
    assertEquals(0, testChildNode.getChildNodes().size());
  }

  @Test
  public void test_addPortfolioTree_addThenGet() {
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(portfolio);
    PortfolioTreeDocument added = _worker.addPortfolioTree(doc);
    
    PortfolioTreeDocument test = _queryWorker.getPortfolioTree(added.getPortfolioId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
