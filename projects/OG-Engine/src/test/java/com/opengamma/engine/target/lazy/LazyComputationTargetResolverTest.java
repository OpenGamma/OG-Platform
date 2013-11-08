/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LazyComputationTargetResolver} class and the stub objects it uses.
 */
@Test(groups = TestGroup.UNIT, enabled = false, description = "FAILING")
public class LazyComputationTargetResolverTest {

  public void testPortfolioNode() {
    final MockComputationTargetResolver mock = MockComputationTargetResolver.unresolved();
    final ComputationTargetResolver resolver = new LazyComputationTargetResolver(mock);
    final ComputationTarget target = resolver.resolve(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "0")), VersionCorrection.LATEST);
    final PortfolioNode node = target.getPortfolioNode();
    assertEquals(node.getUniqueId(), UniqueId.of("Node", "0"));
    assertEquals(mock.getResolveCalls(), 0);
    assertEquals(node.getName(), "Node 0");
    assertEquals(mock.getResolveCalls(), 1);
    final List<PortfolioNode> childNodes = node.getChildNodes();
    assertEquals(childNodes.size(), 2);
    assertEquals(mock.getResolveCalls(), 1);
    assertEquals(childNodes.get(0).getUniqueId(), UniqueId.of("Node", "1"));
    assertEquals(childNodes.get(1).getUniqueId(), UniqueId.of("Node", "4"));
    assertEquals(mock.getResolveCalls(), 1);
    assertEquals(childNodes.get(0).getName(), "Node 1");
    assertEquals(childNodes.get(0).getChildNodes().size(), 2);
    assertEquals(childNodes.get(1).getName(), "Node 4");
    assertEquals(childNodes.get(1).getChildNodes().size(), 2);
    assertEquals(mock.getResolveCalls(), 3);
    final List<Position> positions = node.getPositions();
    assertEquals(positions.size(), 2);
    assertEquals(mock.getResolveCalls(), 3);
    assertEquals(positions.get(0).getUniqueId(), UniqueId.of("Position", "12"));
    assertEquals(positions.get(1).getUniqueId(), UniqueId.of("Position", "13"));
    assertEquals(mock.getResolveCalls(), 3);
    assertEquals(positions.get(0).getQuantity(), BigDecimal.ONE);
    assertEquals(positions.get(1).getQuantity(), BigDecimal.ONE);
    assertEquals(mock.getResolveCalls(), 5);
  }

  public void testPosition() {
    final MockComputationTargetResolver mock = MockComputationTargetResolver.unresolved();
    final ComputationTargetResolver resolver = new LazyComputationTargetResolver(mock);
    final ComputationTarget target = resolver.resolve(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Position", "0")), VersionCorrection.LATEST);
    final Position position = target.getPosition();
    assertEquals(position.getUniqueId(), UniqueId.of("Position", "0"));
    assertEquals(mock.getResolveCalls(), 0);
    assertEquals(position.getAttributes(), Collections.emptyMap());
    assertEquals(position.getQuantity(), BigDecimal.ONE);
    assertEquals(mock.getResolveCalls(), 1);
    final Collection<Trade> trades = position.getTrades();
    assertEquals(trades.size(), 1);
    assertEquals(mock.getResolveCalls(), 1);
    final Trade trade = trades.iterator().next();
    assertEquals(trade.getUniqueId(), UniqueId.of("Trade", "0"));
    assertEquals(mock.getResolveCalls(), 1);
    assertEquals(trade.getQuantity(), BigDecimal.ONE);
    assertEquals(mock.getResolveCalls(), 2);
    final Security security = position.getSecurityLink().getTarget();
    assertEquals(security.getSecurityType(), "MOCK");
    assertEquals(mock.getResolveCalls(), 3);
  }

  public void testTrade() {
    final MockComputationTargetResolver mock = MockComputationTargetResolver.unresolved();
    final ComputationTargetResolver resolver = new LazyComputationTargetResolver(mock);
    final ComputationTarget target = resolver.resolve(new ComputationTargetSpecification(ComputationTargetType.TRADE, UniqueId.of("Trade", "0")), VersionCorrection.LATEST);
    final Trade trade = target.getTrade();
    assertEquals(trade.getUniqueId(), UniqueId.of("Trade", "0"));
    assertEquals(mock.getResolveCalls(), 0);
    assertEquals(trade.getAttributes(), Collections.emptyMap());
    trade.setAttributes(ImmutableMap.of("K1", "V1"));
    assertEquals(trade.getAttributes(), ImmutableMap.of("K1", "V1"));
    trade.addAttribute("K2", "V2");
    assertEquals(trade.getAttributes(), ImmutableMap.of("K1", "V1", "K2", "V2"));
    assertEquals(trade.getCounterparty(), new SimpleCounterparty(ExternalId.of("Counterparty", "Mock")));
    assertEquals(trade.getPremium(), null);
    assertEquals(trade.getPremiumCurrency(), null);
    assertEquals(trade.getPremiumDate(), null);
    assertEquals(trade.getPremiumTime(), null);
    assertEquals(trade.getTradeDate(), MockComputationTargetResolver.TODAY);
    assertEquals(trade.getTradeTime(), null);
    assertEquals(trade.getQuantity(), BigDecimal.ONE);
    assertEquals(mock.getResolveCalls(), 1);
    final Security security = trade.getSecurityLink().getTarget();
    assertEquals(security.getSecurityType(), "MOCK");
    assertEquals(mock.getResolveCalls(), 2);
  }

  public void testSecurity() {
    final MockComputationTargetResolver mock = MockComputationTargetResolver.unresolved();
    final ComputationTargetResolver resolver = new LazyComputationTargetResolver(mock);
    final ComputationTarget target = resolver.resolve(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "0")), VersionCorrection.LATEST);
    final Security security = target.getSecurity();
    assertEquals(mock.getResolveCalls(), 1);
    assertEquals(security.getSecurityType(), "MOCK");
    assertEquals(mock.getResolveCalls(), 1);
  }

}
