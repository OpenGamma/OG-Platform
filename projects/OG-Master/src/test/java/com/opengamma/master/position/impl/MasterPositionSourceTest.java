/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterPositionSource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterPositionSourceTest {

  private static final UniqueId UID = UniqueId.of("A", "B");
  private static final UniqueId UID2 = UniqueId.of("C", "D");
  private static final UniqueId UID3 = UniqueId.of("E", "F");
  private static final UniqueId UID4 = UniqueId.of("G", "H");
  private static final UniqueId UID5 = UniqueId.of("I", "J");
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_3arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null);
  }

  //-------------------------------------------------------------------------
  public void test_getPortfolio_UniqueId() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = example(false);
    ManageablePortfolio manPrt = new ManageablePortfolio("Hello", manNode);
    manPrt.addAttribute("A1", "V1");
    manPrt.addAttribute("A2", "V2");
    manPrt.setUniqueId(UID);
    
    PortfolioDocument prtDoc = new PortfolioDocument(manPrt);
    
    when(mockPortfolio.get(UID)).thenReturn(prtDoc);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    Portfolio testResult = test.getPortfolio(UID, VersionCorrection.LATEST);
    verify(mockPortfolio, times(1)).get(UID);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Hello", testResult.getName());
    assertEquals("Node", testResult.getRootNode().getName());
    assertEquals(UID2, testResult.getRootNode().getUniqueId());
    assertEquals(null, testResult.getRootNode().getParentNodeId());
    assertEquals(0, testResult.getRootNode().getPositions().size());
    assertEquals(1, testResult.getRootNode().getChildNodes().size());
    assertEquals("Child", testResult.getRootNode().getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getRootNode().getChildNodes().get(0).getUniqueId());
    assertEquals(UID2, testResult.getRootNode().getChildNodes().get(0).getParentNodeId());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getChildNodes().size());
    
    assertNotNull(testResult.getAttributes());
    assertEquals(2, testResult.getAttributes().size());
    assertEquals("V1", testResult.getAttributes().get("A1"));
    assertEquals("V2", testResult.getAttributes().get("A2"));
  }

  public void test_getPortfolio_UniqueId_instants_children() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = example(true);
    ManageablePortfolio manPrt = new ManageablePortfolio("Hello", manNode);
    manPrt.setUniqueId(UID);
    PortfolioDocument prtDoc = new PortfolioDocument(manPrt);
    
    ManageableTrade manTrade = new ManageableTrade();
    manTrade.setQuantity(BigDecimal.valueOf(1234));
    manTrade.setSecurityLink(new ManageableSecurityLink(ExternalId.of("CC", "DD")));
    manTrade.setUniqueId(UID5);
    ManageablePosition manPos = new ManageablePosition();
    manPos.setQuantity(BigDecimal.valueOf(1235));
    manPos.setSecurityLink(new ManageableSecurityLink(ExternalId.of("AA", "BB")));
    manPos.setUniqueId(UID4);
    manPos.addTrade(manTrade);
    PositionDocument posDoc = new PositionDocument(manPos);
    PositionSearchRequest posRequest = new PositionSearchRequest();
    posRequest.addPositionObjectId(UID4);
    posRequest.setVersionCorrection(VC);
    PositionSearchResult posResult = new PositionSearchResult();
    posResult.getDocuments().add(posDoc);
    
    when(mockPortfolio.get(UID)).thenReturn(prtDoc);
    when(mockPosition.search(posRequest)).thenReturn(posResult);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    Portfolio testResult = test.getPortfolio(UID, VC);
    verify(mockPortfolio, times(1)).get(UID);
    verify(mockPosition, times(1)).search(posRequest);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Hello", testResult.getName());
    assertEquals("Node", testResult.getRootNode().getName());
    assertEquals(UID2, testResult.getRootNode().getUniqueId());
    assertEquals(null, testResult.getRootNode().getParentNodeId());
    assertEquals(0, testResult.getRootNode().getPositions().size());
    assertEquals(1, testResult.getRootNode().getChildNodes().size());
    assertEquals("Child", testResult.getRootNode().getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getRootNode().getChildNodes().get(0).getUniqueId());
    assertEquals(UID2, testResult.getRootNode().getChildNodes().get(0).getParentNodeId());
    assertEquals(1, testResult.getRootNode().getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getChildNodes().size());
    Position pos = testResult.getRootNode().getChildNodes().get(0).getPositions().get(0);
    assertEquals(UID4, pos.getUniqueId());
    assertEquals(BigDecimal.valueOf(1235), pos.getQuantity());
    assertEquals(ExternalIdBundle.of("AA", "BB"), pos.getSecurityLink().getExternalId());
    assertEquals(1, pos.getTrades().size());
    Trade trade = pos.getTrades().iterator().next();
    assertEquals(UID5, trade.getUniqueId());
    assertEquals(BigDecimal.valueOf(1234), trade.getQuantity());
    assertEquals(ExternalIdBundle.of("CC", "DD"), trade.getSecurityLink().getExternalId());
  }

  //-------------------------------------------------------------------------
  public void test_getPortfolioNode_UniqueId() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = new ManageablePortfolioNode("Node");
    manNode.setUniqueId(UID2);
    manNode.setPortfolioId(UID);
    ManageablePortfolioNode manChild = new ManageablePortfolioNode("Child");
    manChild.setUniqueId(UID3);
    manChild.setParentNodeId(UID2);
    manChild.setPortfolioId(UID);
    manNode.addChildNode(manChild);
    
    when(mockPortfolio.getNode(UID2)).thenReturn(manNode);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    PortfolioNode testResult = test.getPortfolioNode(UID2, VersionCorrection.LATEST);
    verify(mockPortfolio, times(1)).getNode(UID2);
    
    assertEquals("Node", testResult.getName());
    assertEquals(UID2, testResult.getUniqueId());
    assertEquals(null, testResult.getParentNodeId());
    assertEquals(0, testResult.getPositions().size());
    assertEquals(1, testResult.getChildNodes().size());
    assertEquals("Child", testResult.getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getChildNodes().get(0).getUniqueId());
    assertEquals(UID2, testResult.getChildNodes().get(0).getParentNodeId());
    assertEquals(0, testResult.getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getChildNodes().get(0).getChildNodes().size());
  }

  public void test_getPosition() throws Exception {
    final PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    final PositionMaster mockPosition = mock(PositionMaster.class);
    final PositionDocument doc = new PositionDocument(new ManageablePosition(BigDecimal.TEN, ExternalId.of("B", "C")));
    doc.getPosition().setUniqueId(UID);
    when(mockPosition.get(UID)).thenReturn(doc);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    Position testResult = test.getPosition(UID);
    verify(mockPosition, times(1)).get(UID);
    assertEquals(UID, testResult.getUniqueId());
    assertEquals(BigDecimal.TEN, testResult.getQuantity());
    assertEquals(ExternalId.of("B", "C").toBundle(), testResult.getSecurityLink().getExternalId());
  }

  public void test_getTrade() throws Exception {
    final PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    final PositionMaster mockPosition = mock(PositionMaster.class);
    final OffsetDateTime now = OffsetDateTime.now();
    final ManageableTrade doc = new ManageableTrade(BigDecimal.TEN, ExternalId.of("B", "C"), now.toLocalDate(), now.toOffsetTime().minusSeconds(100), ExternalId.of("CPARTY", "C100"));
    doc.setUniqueId(UID);
    when(mockPosition.getTrade(UID)).thenReturn(doc);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    Trade testResult = test.getTrade(UID);
    verify(mockPosition, times(1)).getTrade(UID);
    assertEquals(UID, testResult.getUniqueId());
    assertEquals(BigDecimal.TEN, testResult.getQuantity());
    assertEquals(ExternalId.of("B", "C").toBundle(), testResult.getSecurityLink().getExternalId());
  }

  //-------------------------------------------------------------------------
  protected ManageablePortfolioNode example(boolean withPosition) {
    ManageablePortfolioNode manNode = new ManageablePortfolioNode("Node");
    manNode.setUniqueId(UID2);
    manNode.setPortfolioId(UID);
    ManageablePortfolioNode manChild = new ManageablePortfolioNode("Child");
    manChild.setUniqueId(UID3);
    manChild.setParentNodeId(UID2);
    manChild.setPortfolioId(UID);
    if (withPosition) {
      manChild.addPosition(UID4);
    }
    manNode.addChildNode(manChild);
    return manNode;
  }

}
