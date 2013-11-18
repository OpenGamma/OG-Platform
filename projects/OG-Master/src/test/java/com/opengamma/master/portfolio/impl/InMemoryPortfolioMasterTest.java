/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link InMemoryPortfolioMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryPortfolioMasterTest {

  private static final ObjectIdSupplier POSITION_ID_SUPPLIER = new ObjectIdSupplier("Pos");
  
  private InMemoryPortfolioMaster _emptyMaster;
  private InMemoryPortfolioMaster _populatedMaster;
  private PortfolioDocument _prt1;
  private PortfolioDocument _prt2;
  private PortfolioDocument _prt3;
  
  @BeforeMethod
  public void setUp() {
    _emptyMaster = new InMemoryPortfolioMaster();
    _populatedMaster = new InMemoryPortfolioMaster();
    
    _prt1 = _populatedMaster.add(new PortfolioDocument(generatePortfolio("Port1")));
    _prt2 = _populatedMaster.add(new PortfolioDocument(generatePortfolio("Port2")));
    _prt3 = _populatedMaster.add(new PortfolioDocument(generatePortfolio("Port3")));
  }
  
  private static ManageablePortfolio generatePortfolio() {
    return generatePortfolio("Test");
  }
  
  private static ManageablePortfolio generatePortfolio(String name) {
    ManageablePortfolioNode rootNode = generatePortfolioNodes(name, 2, 2);
    return new ManageablePortfolio(name, rootNode);
  }
  
  private static ManageablePortfolioNode generatePortfolioNodes(String namePrefix, int width, int depth) {
    ManageablePortfolioNode root = new ManageablePortfolioNode(namePrefix);
    root.addPosition(POSITION_ID_SUPPLIER.get());
    if (depth > 0) {
      for (int i = 0; i < width; i++) {
        root.addChildNode(generatePortfolioNodes(namePrefix + "-" + depth + "-" + i, width, depth - 1));
      }
    }
    return root;
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryPortfolioMaster((Supplier<ObjectId>) null);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_defaultSupplier() {
    InMemoryPortfolioMaster master = new InMemoryPortfolioMaster();
    PortfolioDocument added = master.add(new PortfolioDocument(generatePortfolio()));
    assertEquals("MemPrt", added.getUniqueId().getScheme());
  }
  
  @Test
  public void test_alternateSupplier() {
    InMemoryPortfolioMaster master = new InMemoryPortfolioMaster(new ObjectIdSupplier("Hello"));
    PortfolioDocument added = master.add(new PortfolioDocument(generatePortfolio()));
    assertEquals("Hello", added.getUniqueId().getScheme());
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_add_emptyMaster() {
    ManageablePortfolio origPortfolio = generatePortfolio();
    PortfolioDocument addedDoc = _emptyMaster.add(new PortfolioDocument(origPortfolio));
    
    assertNotNull(addedDoc.getVersionFromInstant());
    
    assertEquals("MemPrt", addedDoc.getUniqueId().getScheme());
    assertEquals("1", addedDoc.getUniqueId().getValue());
    
    ManageablePortfolio addedPortfolio = addedDoc.getPortfolio();
    assertNotNull(addedPortfolio);
    assertSame(origPortfolio, addedPortfolio);
    assertEquals(addedDoc.getUniqueId(), addedPortfolio.getUniqueId());    
    assertAddedPortfolioNodes(addedPortfolio.getRootNode(), addedPortfolio.getUniqueId(), null);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    PortfolioDocument doc = new PortfolioDocument(generatePortfolio());
    doc.setUniqueId(UniqueId.of("MemPos", "1"));
    _emptyMaster.update(doc);
  }
  
  public void test_update_populatedMaster() {
    PortfolioDocument doc = new PortfolioDocument(generatePortfolio("updated"));
    doc.setUniqueId(_prt1.getUniqueId());
    PortfolioDocument updated = _populatedMaster.update(doc);
    
    assertEquals(_prt1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_prt1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    _emptyMaster.get(UniqueId.of("MemPrt", "1"));
  }
  
  public void test_get_populatedMaster() {
    PortfolioDocument storedDoc1 = _populatedMaster.get(_prt1.getUniqueId());
    assertNotSame(_prt1, storedDoc1);
    assertEquals(_prt1, storedDoc1);
    assertStoredPortfolioNodes(storedDoc1.getPortfolio().getRootNode(), _prt1.getPortfolio().getRootNode());
  }
  
  public void test_getIsClone() {
    assertNotSame(_populatedMaster.get(_prt1.getUniqueId()), _populatedMaster.get(_prt1.getUniqueId()));
    assertEquals(_populatedMaster.get(_prt1.getUniqueId()), _populatedMaster.get(_prt1.getUniqueId()));
  }
  
  public void test_getIsNotClone() {
    _populatedMaster.setCloneResults(false);
    assertSame(_populatedMaster.get(_prt1.getUniqueId()), _populatedMaster.get(_prt1.getUniqueId()));
  }
  
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getNode_emptyMaster() {
    _emptyMaster.getNode(UniqueId.of("MemPrt", "1"));
  }
  
  public void test_getNode() {
    ManageablePortfolioNode prt1Root = _prt1.getPortfolio().getRootNode();
    ManageablePortfolioNode storedPrt1Root = _populatedMaster.getNode(prt1Root.getUniqueId());
    assertNotSame(prt1Root, storedPrt1Root);
    assertEquals(prt1Root, storedPrt1Root);
    
    ManageablePortfolioNode prt1RootChild1 = prt1Root.getChildNodes().get(0);
    ManageablePortfolioNode storedPrt1RootChild1 = _populatedMaster.getNode(prt1RootChild1.getUniqueId());
    assertNotSame(prt1RootChild1, storedPrt1RootChild1);
    assertEquals(prt1RootChild1, storedPrt1RootChild1);
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    _emptyMaster.remove(UniqueId.of("MemPrt", "1"));
  }
  
  public void test_remove_populatedMaster() {
    _populatedMaster.remove(_prt1.getUniqueId());
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    assertEquals(2, result.getDocuments().size());
    assertTrue(result.getDocuments().contains(_prt2));
    assertTrue(result.getDocuments().contains(_prt3));
  }
  
  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    PortfolioSearchResult result = _emptyMaster.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());    
  }
  
  public void test_search_populatedMaster_all() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();    
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    assertEquals(3, result.getDocuments().size());
    assertTrue(result.getDocuments().contains(_prt1));
    assertTrue(result.getDocuments().contains(_prt2));
    assertTrue(result.getDocuments().contains(_prt3));
  }
  
  public void test_search_populatedMaster_name() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(_prt1.getValue().getName());
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertTrue(result.getDocuments().contains(_prt1));
    assertFalse(result.getDocuments().contains(_prt2));
    assertFalse(result.getDocuments().contains(_prt3));
  }
  
  public void test_search_populatedMaster_nameWildcardStar() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("Port*");
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    assertEquals(3, result.getDocuments().size());
    assertTrue(result.getDocuments().contains(_prt1));
    assertTrue(result.getDocuments().contains(_prt2));
    assertTrue(result.getDocuments().contains(_prt3));
  }
  
  public void test_search_populatedMaster_nameWildcardQuestion() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("Port?");
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    assertEquals(3, result.getDocuments().size());
    assertTrue(result.getDocuments().contains(_prt1));
    assertTrue(result.getDocuments().contains(_prt2));
    assertTrue(result.getDocuments().contains(_prt3));
  }
  
  public void test_search_filterByNodeId() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    ManageablePortfolioNode prt1Root = _prt1.getPortfolio().getRootNode();
    ManageablePortfolioNode prt1RootChild1 = prt1Root.getChildNodes().get(0);
    ManageablePortfolioNode prt1RootChild1Child1 = prt1RootChild1.getChildNodes().get(0);
    request.addNodeObjectId(prt1RootChild1Child1.getUniqueId().getObjectId());
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_prt1, result.getFirstDocument());
  }
  
  public void test_search_filterByNodeId_noMatch() {
    PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addNodeObjectId(ObjectId.of("MemPrt", "Unknown"));
    PortfolioSearchResult result = _populatedMaster.search(request);
    assertEquals(0, result.getDocuments().size());
  }
  
  //-------------------------------------------------------------------------
  private void assertAddedPortfolioNodes(ManageablePortfolioNode node, UniqueId portfolioId, UniqueId parentNodeId) {
    assertNotNull(node.getUniqueId());
    assertNotNull(node.getPortfolioId());
    assertEquals(portfolioId, node.getPortfolioId());
    if (parentNodeId == null) {
      assertNull(node.getParentNodeId());
    } else {
      assertNotNull(node.getParentNodeId());
      assertEquals(parentNodeId, node.getParentNodeId());
    }
    for (int i = 0; i < node.getChildNodes().size(); i++) {
      assertAddedPortfolioNodes(node.getChildNodes().get(i), portfolioId, node.getUniqueId());
    }
  }
  
  private void assertStoredPortfolioNodes(ManageablePortfolioNode storedNode, ManageablePortfolioNode origNode) {
    assertNotSame(origNode.toString() + " vs " + storedNode.toString(), origNode, storedNode);
    for (int i = 0; i < origNode.getChildNodes().size(); i++) {
      assertStoredPortfolioNodes(storedNode.getChildNodes().get(i), origNode.getChildNodes().get(i));
    }
  }
  
}
