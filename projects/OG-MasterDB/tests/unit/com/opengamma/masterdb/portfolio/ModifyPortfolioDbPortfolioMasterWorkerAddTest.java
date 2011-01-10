/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

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
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
public class ModifyPortfolioDbPortfolioMasterWorkerAddTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerAddTest.class);

  private ModifyPortfolioDbPortfolioMasterWorker _worker;
  private QueryPortfolioDbPortfolioMasterWorker _queryWorker;

  public ModifyPortfolioDbPortfolioMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyPortfolioDbPortfolioMasterWorker();
    _worker.init(_prtMaster);
    _queryWorker = new QueryPortfolioDbPortfolioMasterWorker();
    _queryWorker.init(_prtMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_add_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noPortfolio() {
    PortfolioDocument doc = new PortfolioDocument();
    _worker.add(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noRootNode() {
    ManageablePortfolio mockPortfolio = mock(ManageablePortfolio.class);
    when(mockPortfolio.getName()).thenReturn("Test");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(mockPortfolio);
    _worker.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    childNode.addPosition(UniqueIdentifier.of("TestPos", "1234"));
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    PortfolioDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbPrt", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    
    ManageablePortfolio testPortfolio = test.getPortfolio();
    assertEquals(uid, testPortfolio.getUniqueId());
    assertEquals("Test", testPortfolio.getName());
    
    ManageablePortfolioNode testRootNode = testPortfolio.getRootNode();
    assertEquals("Root", testRootNode.getName());
    assertEquals(null, testRootNode.getParentNodeId());
    assertEquals(uid, testRootNode.getPortfolioId());
    assertEquals(1, testRootNode.getChildNodes().size());
    
    ManageablePortfolioNode testChildNode = testRootNode.getChildNodes().get(0);
    assertEquals("Child", testChildNode.getName());
    assertEquals(testRootNode.getUniqueId(), testChildNode.getParentNodeId());
    assertEquals(uid, testChildNode.getPortfolioId());
    assertEquals(0, testChildNode.getChildNodes().size());
    assertEquals(1, testChildNode.getPositionIds().size());
    assertEquals(UniqueIdentifier.of("TestPos", "1234"), testChildNode.getPositionIds().get(0));
  }

  @Test
  public void test_add_addThenGet() {
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    PortfolioDocument added = _worker.add(doc);
    
    PortfolioDocument test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPrt]", _worker.toString());
  }

}
