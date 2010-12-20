/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.time.Instant;

import org.junit.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Test MasterPositionSource.
 */
public class MasterPositionSourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final UniqueIdentifier UID2 = UniqueIdentifier.of("C", "D");
  private static final UniqueIdentifier UID3 = UniqueIdentifier.of("E", "F");
  private static final UniqueIdentifier UID4 = UniqueIdentifier.of("G", "H");
  private static final UniqueIdentifier UID5 = UniqueIdentifier.of("I", "J");
  private static final Instant VERSION_AS_OF = Instant.now();
  private static final Instant CORRECTED_TO = Instant.now();

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_3arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor4arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getPortfolio_uid() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = new ManageablePortfolioNode("Node");
    manNode.setUniqueIdentifier(UID2);
    manNode.setPortfolioId(UID);
    ManageablePortfolioNode manChild = new ManageablePortfolioNode("Child");
    manChild.setUniqueIdentifier(UID3);
    manChild.setParentNodeId(UID2);
    manChild.setPortfolioId(UID);
    manNode.addChildNode(manChild);
    ManageablePortfolio manPrt = new ManageablePortfolio("Hello", manNode);
    manPrt.setUniqueIdentifier(UID);
    PortfolioDocument prtDoc = new PortfolioDocument(manPrt);
    
    when(mockPortfolio.get(UID)).thenReturn(prtDoc);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    Portfolio testResult = test.getPortfolio(UID);
    verify(mockPortfolio, times(1)).get(UID);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals("Hello", testResult.getName());
    assertEquals("Node", testResult.getRootNode().getName());
    assertEquals(UID2, testResult.getRootNode().getUniqueIdentifier());
    assertEquals(null, testResult.getRootNode().getParentNode());
    assertEquals(0, testResult.getRootNode().getPositions().size());
    assertEquals(1, testResult.getRootNode().getChildNodes().size());
    assertEquals("Child", testResult.getRootNode().getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getRootNode().getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(UID2, testResult.getRootNode().getChildNodes().get(0).getParentNode());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getChildNodes().size());
  }

  @Test
  public void test_getPortfolio_oid_instants_children() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = new ManageablePortfolioNode("Node");
    manNode.setUniqueIdentifier(UID2);
    manNode.setPortfolioId(UID);
    ManageablePortfolioNode manChild = new ManageablePortfolioNode("Child");
    manChild.setUniqueIdentifier(UID3);
    manChild.setParentNodeId(UID2);
    manChild.setPortfolioId(UID);
    manChild.addPosition(UID4);
    manNode.addChildNode(manChild);
    ManageablePortfolio manPrt = new ManageablePortfolio("Hello", manNode);
    manPrt.setUniqueIdentifier(UID);
    PortfolioDocument prtDoc = new PortfolioDocument(manPrt);
    PortfolioHistoryRequest portfolioRequest = new PortfolioHistoryRequest(UID.toLatest(), VERSION_AS_OF, CORRECTED_TO);
    PortfolioHistoryResult portfolioResult = new PortfolioHistoryResult();
    portfolioResult.getDocuments().add(prtDoc);
    
    ManageableTrade manTrade = new ManageableTrade();
    manTrade.setQuantity(BigDecimal.valueOf(1234));
    manTrade.setSecurityKey(IdentifierBundle.of(Identifier.of("CC", "DD")));
    manTrade.setUniqueIdentifier(UID5);
    manTrade.setPositionId(UID4);
    ManageablePosition manPos = new ManageablePosition();
    manPos.setQuantity(BigDecimal.valueOf(1235));
    manPos.setSecurityKey(IdentifierBundle.of(Identifier.of("AA", "BB")));
    manPos.setUniqueIdentifier(UID4);
    manPos.addTrade(manTrade);
    PositionDocument posDoc = new PositionDocument(manPos);
    PositionSearchRequest posRequest = new PositionSearchRequest();
    posRequest.addPositionId(UID4);
    posRequest.setVersionAsOfInstant(VERSION_AS_OF);
    posRequest.setCorrectedToInstant(CORRECTED_TO);
    PositionSearchResult posResult = new PositionSearchResult();
    posResult.getDocuments().add(posDoc);
    
    when(mockPortfolio.history(portfolioRequest)).thenReturn(portfolioResult);
    when(mockPosition.search(posRequest)).thenReturn(posResult);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition, VERSION_AS_OF, CORRECTED_TO);
    Portfolio testResult = test.getPortfolio(UID);
    verify(mockPortfolio, times(1)).history(portfolioRequest);
    verify(mockPosition, times(1)).search(posRequest);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals("Hello", testResult.getName());
    assertEquals("Node", testResult.getRootNode().getName());
    assertEquals(UID2, testResult.getRootNode().getUniqueIdentifier());
    assertEquals(null, testResult.getRootNode().getParentNode());
    assertEquals(0, testResult.getRootNode().getPositions().size());
    assertEquals(1, testResult.getRootNode().getChildNodes().size());
    assertEquals("Child", testResult.getRootNode().getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getRootNode().getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(UID2, testResult.getRootNode().getChildNodes().get(0).getParentNode());
    assertEquals(1, testResult.getRootNode().getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getRootNode().getChildNodes().get(0).getChildNodes().size());
    Position pos = testResult.getRootNode().getChildNodes().get(0).getPositions().get(0);
    UniqueIdentifier combinedUid4 = UniqueIdentifier.of(UID3.getScheme() + "-" + UID4.getScheme(), UID3.getValue() + "-" + UID4.getValue(), "-");
    UniqueIdentifier combinedUid5 = UniqueIdentifier.of(UID3.getScheme() + "-" + UID5.getScheme(), UID3.getValue() + "-" + UID5.getValue(), "-");
    assertEquals(combinedUid4, pos.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(1235), pos.getQuantity());
    assertEquals(IdentifierBundle.of(Identifier.of("AA", "BB")), pos.getSecurityKey());
    assertEquals(1, pos.getTrades().size());
    Trade trade = pos.getTrades().iterator().next();
    assertEquals(combinedUid5, trade.getUniqueIdentifier());
    assertEquals(combinedUid4, trade.getPositionId());
    assertEquals(BigDecimal.valueOf(1234), trade.getQuantity());
    assertEquals(IdentifierBundle.of(Identifier.of("CC", "DD")), trade.getSecurityKey());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getPortfolioNode_uid() throws Exception {
    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
    PositionMaster mockPosition = mock(PositionMaster.class);
    
    ManageablePortfolioNode manNode = new ManageablePortfolioNode("Node");
    manNode.setUniqueIdentifier(UID2);
    manNode.setPortfolioId(UID);
    ManageablePortfolioNode manChild = new ManageablePortfolioNode("Child");
    manChild.setUniqueIdentifier(UID3);
    manChild.setParentNodeId(UID2);
    manChild.setPortfolioId(UID);
    manNode.addChildNode(manChild);
    
    when(mockPortfolio.getNode(UID2)).thenReturn(manNode);
    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition);
    PortfolioNode testResult = test.getPortfolioNode(UID2);
    verify(mockPortfolio, times(1)).getNode(UID2);
    
    assertEquals("Node", testResult.getName());
    assertEquals(UID2, testResult.getUniqueIdentifier());
    assertEquals(null, testResult.getParentNode());
    assertEquals(0, testResult.getPositions().size());
    assertEquals(1, testResult.getChildNodes().size());
    assertEquals("Child", testResult.getChildNodes().get(0).getName());
    assertEquals(UID3, testResult.getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(UID2, testResult.getChildNodes().get(0).getParentNode());
    assertEquals(0, testResult.getChildNodes().get(0).getPositions().size());
    assertEquals(0, testResult.getChildNodes().get(0).getChildNodes().size());
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_getPosition() throws Exception {
//    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
//    PositionMaster mockPosition = mock(PositionMaster.class);
//    FullPositionGetRequest request = new FullPositionGetRequest(UID);
//    Instant now = Instant.now();
//    request.setVersionAsOfInstant(now.minusSeconds(2));
//    request.setCorrectedToInstant(now.minusSeconds(1));
//    Position node = new PositionImpl(UID, BigDecimal.TEN, Identifier.of("B", "C"));
//    
//    when(mockPortfolio.getFullPosition(request)).thenReturn(node);
//    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition, now.minusSeconds(2), now.minusSeconds(1));
//    Position testResult = test.getPosition(UID);
//    verify(mockPortfolio, times(1)).getFullPosition(request);
//    
//    assertEquals(UID, testResult.getUniqueIdentifier());
//    assertEquals(BigDecimal.TEN, testResult.getQuantity());
//    assertEquals(Identifier.of("B", "C"), testResult.getSecurityKey().getIdentifiers().iterator().next());
//  }
//  
//  @Test
//  public void test_getTrade() throws Exception {
//    PortfolioMaster mockPortfolio = mock(PortfolioMaster.class);
//    PositionMaster mockPosition = mock(PositionMaster.class);
//    FullTradeGetRequest request = new FullTradeGetRequest(UID);
//
//    OffsetDateTime now = OffsetDateTime.now();
//    final UniqueIdentifier positionId = UniqueIdentifier.of("P", "A");
//    final MockSecurity security = new MockSecurity("A");
//    security.setIdentifiers(IdentifierBundle.of(Identifier.of("S", "A")));
//    final Counterparty counterparty = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
//    
//    TradeImpl trade = new TradeImpl(positionId, security, BigDecimal.TEN, counterparty, now.toLocalDate(), now.toOffsetTime().minusSeconds(100));
//    trade.setUniqueIdentifier(UID);
//    
//    when(mockPortfolio.getFullTrade(request)).thenReturn(trade);
//    
//    MasterPositionSource test = new MasterPositionSource(mockPortfolio, mockPosition, now.minusSeconds(2), now.minusSeconds(1));
//    Trade testResult = test.getTrade(UID);
//    verify(mockPortfolio, times(1)).getFullTrade(request);
//    
//    assertEquals(UID, testResult.getUniqueIdentifier());
//    assertEquals(BigDecimal.TEN, testResult.getQuantity());
//    assertEquals(Identifier.of("S", "A"), testResult.getSecurityKey().getIdentifiers().iterator().next());
//    assertEquals(counterparty, testResult.getCounterparty());
//    assertEquals(positionId, testResult.getPositionId());
//    assertEquals(now.toLocalDate(), testResult.getTradeDate());
//  }

}
