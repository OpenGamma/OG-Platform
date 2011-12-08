/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyPortfolioDbPortfolioMasterWorker.
 */
public class ModifyPortfolioDbPortfolioMasterWorkerAddTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPortfolioDbPortfolioMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_nullDocument() {
    _prtMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noPortfolio() {
    PortfolioDocument doc = new PortfolioDocument();
    _prtMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noRootNode() {
    ManageablePortfolio mockPortfolio = mock(ManageablePortfolio.class);
    when(mockPortfolio.getName()).thenReturn("Test");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(mockPortfolio);
    _prtMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    childNode.addPosition(UniqueId.of("TestPos", "1234"));
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    PortfolioDocument test = _prtMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPrt", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(DocumentVisibility.VISIBLE, test.getVisibility());
    
    ManageablePortfolio testPortfolio = test.getPortfolio();
    assertEquals(uniqueId, testPortfolio.getUniqueId());
    assertEquals("Test", testPortfolio.getName());
    assertNotNull(testPortfolio.getAttributes());
    
    ManageablePortfolioNode testRootNode = testPortfolio.getRootNode();
    assertEquals("Root", testRootNode.getName());
    assertEquals(null, testRootNode.getParentNodeId());
    assertEquals(uniqueId, testRootNode.getPortfolioId());
    assertEquals(1, testRootNode.getChildNodes().size());
    
    ManageablePortfolioNode testChildNode = testRootNode.getChildNodes().get(0);
    assertEquals("Child", testChildNode.getName());
    assertEquals(testRootNode.getUniqueId(), testChildNode.getParentNodeId());
    assertEquals(uniqueId, testChildNode.getPortfolioId());
    assertEquals(0, testChildNode.getChildNodes().size());
    assertEquals(1, testChildNode.getPositionIds().size());
    assertEquals(ObjectId.of("TestPos", "1234"), testChildNode.getPositionIds().get(0));
  }
  
  @Test
  public void test_addWithAttributes_add() {
    Instant now = Instant.now(_prtMaster.getTimeSource());
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    childNode.addPosition(UniqueId.of("TestPos", "1234"));
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    portfolio.addAttribute("A1", "V1");
    portfolio.addAttribute("A2", "V2");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    PortfolioDocument test = _prtMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPrt", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    
    ManageablePortfolio testPortfolio = test.getPortfolio();
    assertEquals(uniqueId, testPortfolio.getUniqueId());
    assertEquals("Test", testPortfolio.getName());
    assertNotNull(testPortfolio.getAttributes());
    assertEquals(2, testPortfolio.getAttributes().size());
    assertEquals("V1", testPortfolio.getAttributes().get("A1"));
    assertEquals("V2", testPortfolio.getAttributes().get("A2"));
    
    ManageablePortfolioNode testRootNode = testPortfolio.getRootNode();
    assertEquals("Root", testRootNode.getName());
    assertEquals(null, testRootNode.getParentNodeId());
    assertEquals(uniqueId, testRootNode.getPortfolioId());
    assertEquals(1, testRootNode.getChildNodes().size());
    
    ManageablePortfolioNode testChildNode = testRootNode.getChildNodes().get(0);
    assertEquals("Child", testChildNode.getName());
    assertEquals(testRootNode.getUniqueId(), testChildNode.getParentNodeId());
    assertEquals(uniqueId, testChildNode.getPortfolioId());
    assertEquals(0, testChildNode.getChildNodes().size());
    assertEquals(1, testChildNode.getPositionIds().size());
    assertEquals(ObjectId.of("TestPos", "1234"), testChildNode.getPositionIds().get(0));
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
    PortfolioDocument added = _prtMaster.add(doc);
    
    PortfolioDocument test = _prtMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }
  
  @Test
  public void test_addWithAttributes_addThenGet() {
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    portfolio.addAttribute("A1", "V1");
    portfolio.addAttribute("A2", "V2");
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    PortfolioDocument added = _prtMaster.add(doc);
    
    PortfolioDocument test = _prtMaster.get(added.getUniqueId());
    assertEquals(added, test);
    
    //add another
    portfolio = new ManageablePortfolio("Test2");
    portfolio.setRootNode(rootNode);
    portfolio.addAttribute("A21", "V21");
    portfolio.addAttribute("A22", "V22");
    doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    added = _prtMaster.add(doc);
    
    test = _prtMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }
  
  @Test
  public void test_addCustomVisibility_addThenGet() {
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode("Root");
    ManageablePortfolioNode childNode = new ManageablePortfolioNode("Child");
    rootNode.addChildNode(childNode);
    ManageablePortfolio portfolio = new ManageablePortfolio("Test");
    portfolio.setRootNode(rootNode);
    PortfolioDocument doc = new PortfolioDocument();
    doc.setPortfolio(portfolio);
    doc.setVisibility(DocumentVisibility.HIDDEN);
    PortfolioDocument added = _prtMaster.add(doc);
    
    PortfolioDocument test = _prtMaster.get(added.getUniqueId());
    assertEquals(DocumentVisibility.HIDDEN, test.getVisibility());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_prtMaster.getClass().getSimpleName() + "[DbPrt]", _prtMaster.toString());
  }

}
