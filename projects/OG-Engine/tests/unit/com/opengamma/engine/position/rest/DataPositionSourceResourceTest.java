/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionResource.
 */
public class DataPositionSourceResourceTest {

  private static final UniqueIdentifier UID1 = UniqueIdentifier.of("Test", "A");
  private static final UniqueIdentifier UID2 = UniqueIdentifier.of("Test", "B");
  private PositionSource _underlying;
  private DataPositionSourceResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionSource.class);
    _resource = new DataPositionSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolio() {
    final Position position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    final PortfolioNodeImpl node = new PortfolioNodeImpl(UID1, "TestNode");
    final PortfolioNodeImpl child = new PortfolioNodeImpl(UID1, "Child");
    node.addChildNode(child);
    node.addPosition(position);
    final PortfolioImpl portfolio = new PortfolioImpl(UID1, "TestPortfolio");
    portfolio.setRootNode(node);
    when(_underlying.getPortfolio(eq(UID1))).thenReturn(portfolio);
    
    Response test = _resource.getPortfolio(UID1.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(portfolio, test.getEntity());
  }

  @Test
  public void testGetPortfolioNode() {
    final PortfolioNodeImpl node = new PortfolioNodeImpl(UID1, "TestNode");
    final PortfolioNodeImpl child = new PortfolioNodeImpl(UID1, "Child");
    final Position position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    node.addChildNode(child);
    node.addPosition(position);
    when(_underlying.getPortfolioNode(eq(UID1))).thenReturn(node);
    
    Response test = _resource.getNode(UID1.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(node, test.getEntity());
  }

  @Test
  public void testGetPosition() {
    final PositionImpl position = new PositionImpl(UID1, BigDecimal.TEN, Identifier.of("A", "B"));
    position.setParentNodeId(UID2);
    when(_underlying.getPosition(eq(UID1))).thenReturn(position);
    
    Response test = _resource.getPosition(UID1.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(position, test.getEntity());
  }

  @Test
  public void testGetTrade() {
    final Counterparty cparty = new CounterpartyImpl(Identifier.of("C", "D"));
    final TradeImpl trade = new TradeImpl(UID2, Identifier.of("A", "B"), BigDecimal.TEN, cparty, LocalDate.of(2010, 12, 6), null);
    trade.setUniqueId(UID1);
    when(_underlying.getTrade(eq(UID1))).thenReturn(trade);
    
    Response test = _resource.getTrade(UID1.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(trade, test.getEntity());
  }

}
