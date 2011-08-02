/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.rest;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Tests DataPositionResource.
 */
@Test
public class DataPositionSourceResourceTest {

  private static final UniqueIdentifier UID1 = UniqueIdentifier.of("Test", "A");
  private static final UniqueIdentifier UID2 = UniqueIdentifier.of("Test", "B");
  private PositionSource _underlying;
  private DataPositionSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionSource.class);
    _resource = new DataPositionSourceResource(OpenGammaFudgeContext.getInstance (), _underlying);
  }
  
  private <T> T decodeResponse (final Class<T> clazz, final String field, final FudgeMsgEnvelope response) {
    final FudgeDeserializationContext fdc = new FudgeDeserializationContext (_resource.getFudgeContext ());
    return fdc.fieldValueToObject(clazz, response.getMessage ().getByName(field));
  }

  //-------------------------------------------------------------------------
  public void testGetPortfolio() {
    final Position position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    final PortfolioNodeImpl node = new PortfolioNodeImpl(UID1, "TestNode");
    final PortfolioNodeImpl child = new PortfolioNodeImpl(UID1, "Child");
    node.addChildNode(child);
    node.addPosition(position);
    final PortfolioImpl portfolio = new PortfolioImpl(UID1, "TestPortfolio");
    portfolio.setRootNode(node);
    when(_underlying.getPortfolio(eq(UID1))).thenReturn(portfolio);
    
    assertEquals (portfolio, decodeResponse (Portfolio.class, "portfolio", _resource.getPortfolio(UID1.toString())));
  }

  public void testGetPortfolioNode() {
    final PortfolioNodeImpl node = new PortfolioNodeImpl(UID1, "TestNode");
    final PortfolioNodeImpl child = new PortfolioNodeImpl(UID1, "Child");
    final Position position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    node.addChildNode(child);
    node.addPosition(position);
    when(_underlying.getPortfolioNode(eq(UID1))).thenReturn(node);
    
    assertEquals (node, decodeResponse (PortfolioNode.class, "node", _resource.getNode(UID1.toString())));
  }

  public void testGetPosition() {
    final PositionImpl position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    position.setParentNodeId(UID2);
    final TradeImpl trade = new TradeImpl(position.getUniqueId(), new SecurityLink(Identifier.of("A", "B")), BigDecimal.TEN, new CounterpartyImpl(Identifier.of("Foo", "Bar")), LocalDate.now(), OffsetTime.now());
    position.addTrade(trade);
    when(_underlying.getPosition(eq(UID1))).thenReturn(position);
    
    assertEquals (position, decodeResponse (Position.class, "position", _resource.getPosition(UID1.toString())));
  }

  public void testGetTrade() {
    final Counterparty cparty = new CounterpartyImpl(Identifier.of("C", "D"));
    final TradeImpl trade = new TradeImpl(UID2, new SecurityLink(Identifier.of("A", "B")), BigDecimal.TEN, cparty, LocalDate.of(2010, 12, 6), null);
    trade.setUniqueId(UID1);
    when(_underlying.getTrade(eq(UID1))).thenReturn(trade);
    
    assertEquals (trade, decodeResponse (Trade.class, "trade", _resource.getTrade(UID1.toString())));
  }

}
