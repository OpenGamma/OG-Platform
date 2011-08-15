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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Tests DataPositionResource.
 */
@Test
public class DataPositionSourceResourceTest {

  private static final UniqueId UID1 = UniqueId.of("Test", "A");
  private static final UniqueId UID2 = UniqueId.of("Test", "B");
  private PositionSource _underlying;
  private DataPositionSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionSource.class);
    _resource = new DataPositionSourceResource(OpenGammaFudgeContext.getInstance (), _underlying);
  }
  
  private <T> T decodeResponse (final Class<T> clazz, final String field, final FudgeMsgEnvelope response) {
    final FudgeDeserializer deserializer = new FudgeDeserializer (_resource.getFudgeContext ());
    return deserializer.fieldValueToObject(clazz, response.getMessage ().getByName(field));
  }

  //-------------------------------------------------------------------------
  public void testGetPortfolio() {
    final Position position = new SimplePosition(UID1, BigDecimal.TEN, ExternalId.of("A", "B"));
    final SimplePortfolioNode node = new SimplePortfolioNode(UID1, "TestNode");
    final SimplePortfolioNode child = new SimplePortfolioNode(UID1, "Child");
    node.addChildNode(child);
    node.addPosition(position);
    final SimplePortfolio portfolio = new SimplePortfolio(UID1, "TestPortfolio");
    portfolio.setRootNode(node);
    when(_underlying.getPortfolio(eq(UID1))).thenReturn(portfolio);
    
    assertEquals (portfolio, decodeResponse (Portfolio.class, "portfolio", _resource.getPortfolio(UID1.toString())));
  }

  public void testGetPortfolioNode() {
    final SimplePortfolioNode node = new SimplePortfolioNode(UID1, "TestNode");
    final SimplePortfolioNode child = new SimplePortfolioNode(UID1, "Child");
    final Position position = new SimplePosition(UID1, BigDecimal.TEN, ExternalId.of("A", "B"));
    node.addChildNode(child);
    node.addPosition(position);
    when(_underlying.getPortfolioNode(eq(UID1))).thenReturn(node);
    
    assertEquals (node, decodeResponse (PortfolioNode.class, "node", _resource.getNode(UID1.toString())));
  }

  public void testGetPosition() {
    final SimplePosition position = new SimplePosition(UID1, BigDecimal.TEN, ExternalId.of("A", "B"));
    position.setParentNodeId(UID2);
    final SimpleTrade trade = new SimpleTrade(position.getUniqueId(), new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.TEN, new SimpleCounterparty(ExternalId.of("Foo", "Bar")), LocalDate.now(), OffsetTime.now());
    position.addTrade(trade);
    when(_underlying.getPosition(eq(UID1))).thenReturn(position);
    
    assertEquals (position, decodeResponse (Position.class, "position", _resource.getPosition(UID1.toString())));
  }

  public void testGetTrade() {
    final Counterparty cparty = new SimpleCounterparty(ExternalId.of("C", "D"));
    final SimpleTrade trade = new SimpleTrade(UID2, new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.TEN, cparty, LocalDate.of(2010, 12, 6), null);
    trade.setUniqueId(UID1);
    when(_underlying.getTrade(eq(UID1))).thenReturn(trade);
    
    assertEquals (trade, decodeResponse (Trade.class, "trade", _resource.getTrade(UID1.toString())));
  }

}
