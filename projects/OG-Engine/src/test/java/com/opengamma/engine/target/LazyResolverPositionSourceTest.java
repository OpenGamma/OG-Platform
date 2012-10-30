/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Tests the {@link LazyResolverPositionSource} class and the stub objects it uses.
 */
@Test
public class LazyResolverPositionSourceTest {

  public void testGetPortfolio_byUniqueId() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.unresolved();
    final LazyResolverPositionSource ps = new LazyResolverPositionSource(resolver.getPositionSource(), new LazyResolveContext(resolver.getSecuritySource()));
    final Portfolio portfolio = ps.getPortfolio(UniqueId.of("Portfolio", "0"));
    final Position position = portfolio.getRootNode().getPositions().iterator().next();
    assertEquals(position.getQuantity(), BigDecimal.ONE);
    assertNull(position.getSecurityLink().getTarget());
    assertNotNull(position.getSecurity());
    assertNotNull(position.getSecurityLink().getTarget());
  }

  public void testGetPortfolio_byObjectId() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.unresolved();
    final LazyResolverPositionSource ps = new LazyResolverPositionSource(resolver.getPositionSource(), new LazyResolveContext(resolver.getSecuritySource()));
    final Portfolio portfolio = ps.getPortfolio(ObjectId.of("Portfolio", "0"), VersionCorrection.LATEST);
    final Position position = portfolio.getRootNode().getPositions().iterator().next();
    assertEquals(position.getQuantity(), BigDecimal.ONE);
    assertNull(position.getSecurityLink().getTarget());
    assertNotNull(position.getSecurity());
    assertNotNull(position.getSecurityLink().getTarget());
  }

  public void testGetPortfolioNode_byUniqueId() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.unresolved();
    final LazyResolverPositionSource ps = new LazyResolverPositionSource(resolver.getPositionSource(), new LazyResolveContext(resolver.getSecuritySource()));
    final PortfolioNode node = ps.getPortfolioNode(UniqueId.of("Mock", "0-11"));
    final Position position = node.getPositions().iterator().next();
    assertEquals(position.getQuantity(), BigDecimal.ONE);
    assertNull(position.getSecurityLink().getTarget());
    assertNotNull(position.getSecurity());
    assertNotNull(position.getSecurityLink().getTarget());
  }

  public void testGetPosition_byUniqueId() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.unresolved();
    final LazyResolverPositionSource ps = new LazyResolverPositionSource(resolver.getPositionSource(), new LazyResolveContext(resolver.getSecuritySource()));
    final Position position = ps.getPosition(UniqueId.of("Mock", "0-34"));
    assertEquals(position.getQuantity(), BigDecimal.ONE);
    assertNull(position.getSecurityLink().getTarget());
    assertNotNull(position.getSecurity());
    assertNotNull(position.getSecurityLink().getTarget());
    final Trade trade = position.getTrades().iterator().next();
    assertEquals(trade.getQuantity(), BigDecimal.ONE);
    assertNull(trade.getSecurityLink().getTarget());
    assertNotNull(trade.getSecurity());
    assertNotNull(trade.getSecurityLink().getTarget());
  }

  public void testGetTrade_byUniqueId() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.unresolved();
    final LazyResolverPositionSource ps = new LazyResolverPositionSource(resolver.getPositionSource(), new LazyResolveContext(resolver.getSecuritySource()));
    final Trade trade = ps.getTrade(UniqueId.of("Mock", "0-15"));
    assertEquals(trade.getQuantity(), BigDecimal.ONE);
    assertNull(trade.getSecurityLink().getTarget());
    assertNotNull(trade.getSecurity());
    assertNotNull(trade.getSecurityLink().getTarget());
  }

}
