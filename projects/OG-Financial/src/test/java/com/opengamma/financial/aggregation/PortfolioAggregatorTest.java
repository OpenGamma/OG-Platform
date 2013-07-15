/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioAggregatorTest {

  @Test(enabled = false)
  public void multipleInstancesOfSamePosition() {
    SimplePortfolio portfolio = new SimplePortfolio(id("portfolio"), "portfolio");
    SimplePortfolioNode root = new SimplePortfolioNode(id("root"), "root");
    SimplePortfolioNode node1 = new SimplePortfolioNode(id("node1"), "node1");
    SimplePortfolioNode node2 = new SimplePortfolioNode(id("node2"), "node2");
    ExternalId securityId = ExternalId.of("sec", "123");
    SimplePosition position = new SimplePosition(id("position"), BigDecimal.ONE, securityId);
    SimpleCounterparty counterparty = new SimpleCounterparty(ExternalId.of("cpty", "123"));
    SimpleSecurityLink securityLink = new SimpleSecurityLink(securityId);
    Trade trade = new SimpleTrade(securityLink, BigDecimal.ONE, counterparty, LocalDate.now(), OffsetTime.now());
    position.addTrade(trade);
    portfolio.setRootNode(root);
    node1.addPosition(position);
    node2.addPosition(position);
    root.addChildNode(node1);
    root.addChildNode(node2);

    CounterpartyAggregationFunction fn = new CounterpartyAggregationFunction();
    Portfolio aggregate = new PortfolioAggregator(fn).aggregate(portfolio);
    PortfolioNode aggregateRoot = aggregate.getRootNode();
    assertEquals(1, aggregateRoot.getChildNodes().size());
    PortfolioNode node = aggregateRoot.getChildNodes().get(0);
    assertEquals(1, node.getPositions().size());
  }

  private static UniqueId id(String value) {
    return UniqueId.of("scheme", value);
  }
}
