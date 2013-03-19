/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests that {@link AnalyticsNode.PortfolioNodeBuilder} creates nodes that match a portfolio structure.
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsNodeBuilderTest {

  @Test
  public void emptyPortfolio() {
    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(new SimplePortfolioNode()).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(0, root.getEndRow());
    assertTrue(root.getChildren().isEmpty());
  }

  @Test
  public void flatPortfolio() {
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode("");
    portfolioRoot.addPosition(new SimplePosition());
    portfolioRoot.addPosition(new SimplePosition());
    portfolioRoot.addPosition(new SimplePosition());
    portfolioRoot.addPosition(new SimplePosition());
    portfolioRoot.addPosition(new SimplePosition());

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(5, root.getEndRow()); // 1 node for the root and one each for the positions
    assertTrue(root.getChildren().isEmpty());
  }

  @Test
  public void subNodesAndPositions() {
    /*
    0  root
    1   |_pos
    2   |_pos
    3   |_child1
    4      |_pos
    5      |_pos
    */
    SimplePortfolioNode portfolioChild = new SimplePortfolioNode();
    portfolioChild.addPosition(new SimplePosition());
    portfolioChild.addPosition(new SimplePosition());
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode("");
    portfolioRoot.addChildNode(portfolioChild);
    portfolioRoot.addPosition(new SimplePosition());
    portfolioRoot.addPosition(new SimplePosition());

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(5, root.getEndRow());
    assertEquals(1, root.getChildren().size());
    AnalyticsNode child = root.getChildren().get(0);
    assertEquals(3, child.getStartRow());
    assertEquals(5, child.getEndRow());
    assertTrue(child.getChildren().isEmpty());
  }

  @Test
  public void deeplyNested() {
    /*
    0  root
    1   |_pos
    2   |_child1
    3      |_pos
    4      |_pos
    5      |_child2
    6        |_pos
    7        |_pos
    8        |_child3
    9          |_pos
    10         |_pos
    */
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode();
    portfolioRoot.addPosition(new SimplePosition());
    SimplePortfolioNode portfolioChild1 = new SimplePortfolioNode();
    portfolioChild1.addPosition(new SimplePosition());
    portfolioChild1.addPosition(new SimplePosition());
    SimplePortfolioNode portfolioChild2 = new SimplePortfolioNode();
    portfolioChild2.addPosition(new SimplePosition());
    portfolioChild2.addPosition(new SimplePosition());
    SimplePortfolioNode portfolioChild3 = new SimplePortfolioNode();
    portfolioChild3.addPosition(new SimplePosition());
    portfolioChild3.addPosition(new SimplePosition());
    portfolioRoot.addChildNode(portfolioChild1);
    portfolioChild1.addChildNode(portfolioChild2);
    portfolioChild2.addChildNode(portfolioChild3);

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(10, root.getEndRow());
    assertEquals(1, root.getChildren().size());
    AnalyticsNode child1 = root.getChildren().get(0);
    assertEquals(2, child1.getStartRow());
    assertEquals(10, child1.getEndRow());
    assertEquals(1, child1.getChildren().size());
    AnalyticsNode child2 = child1.getChildren().get(0);
    assertEquals(5, child2.getStartRow());
    assertEquals(10, child2.getEndRow());
    assertEquals(1, child2.getChildren().size());
    AnalyticsNode child3 = child2.getChildren().get(0);
    assertEquals(8, child3.getStartRow());
    assertEquals(10, child3.getEndRow());
    assertTrue(child3.getChildren().isEmpty());
  }

  @Test
  public void multipleChildren() {
    /*
    0 root
    1  |_child1
    2  |  |_pos
    3  |_child2
    4     |_pos
    */
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode();
    SimplePortfolioNode portfolioChild1 = new SimplePortfolioNode();
    portfolioChild1.addPosition(new SimplePosition());
    SimplePortfolioNode portfolioChild2 = new SimplePortfolioNode();
    portfolioChild2.addPosition(new SimplePosition());
    portfolioRoot.addChildNode(portfolioChild1);
    portfolioRoot.addChildNode(portfolioChild2);

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(4, root.getEndRow());
    assertEquals(2, root.getChildren().size());
    AnalyticsNode child1 = root.getChildren().get(0);
    assertEquals(1, child1.getStartRow());
    assertEquals(2, child1.getEndRow());
    assertTrue(child1.getChildren().isEmpty());
    AnalyticsNode child2 = root.getChildren().get(1);
    assertEquals(3, child2.getStartRow());
    assertEquals(4, child2.getEndRow());
    assertTrue(child2.getChildren().isEmpty());
  }

  @Test
  public void fungibleTrades() {
    /*
    0 root
    1  |_pos1
    2  |  |_trade1
    3  |  |_trade2
    4  |_pos2
    5     |_trade3
    */
    EquitySecurity security = new EquitySecurity("exchange", "exchangeCode", "companyName", Currency.USD);
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode();
    SimplePosition position1 = new SimplePosition();
    SimpleSecurityLink securityLink = new SimpleSecurityLink();
    securityLink.setTarget(security);
    position1.setSecurityLink(securityLink);
    position1.addTrade(new SimpleTrade());
    position1.addTrade(new SimpleTrade());
    SimplePosition position2 = new SimplePosition();
    position2.setSecurityLink(securityLink);
    position2.addTrade(new SimpleTrade());
    portfolioRoot.addPosition(position1);
    portfolioRoot.addPosition(position2);

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(5, root.getEndRow());
    assertEquals(2, root.getChildren().size());
    AnalyticsNode position1Node = root.getChildren().get(0);
    assertEquals(1, position1Node.getStartRow());
    assertEquals(3, position1Node.getEndRow());
    assertTrue(position1Node.getChildren().isEmpty());
    AnalyticsNode position2Node = root.getChildren().get(1);
    assertEquals(4, position2Node.getStartRow());
    assertEquals(5, position2Node.getEndRow());
    assertTrue(position2Node.getChildren().isEmpty());
  }

  @Test
  public void otcTrades() {
    /*
    0 root
    1  |_pos/trade1
    2  |_pos/trade2
    */
    FXForwardSecurity security = new FXForwardSecurity(Currency.GBP, 123,
                                                       Currency.USD, 321,
                                                       LocalDate.of(2012, 12, 21).atTime(11, 0).atZone(ZoneOffset.UTC),
                                                       ExternalId.of("Reg", "ABC"));
    SimplePortfolioNode portfolioRoot = new SimplePortfolioNode();
    SimplePosition position1 = new SimplePosition();
    SimpleSecurityLink securityLink = new SimpleSecurityLink();
    securityLink.setTarget(security);
    position1.setSecurityLink(securityLink);
    position1.addTrade(new SimpleTrade());
    SimplePosition position2 = new SimplePosition();
    position2.setSecurityLink(securityLink);
    position2.addTrade(new SimpleTrade());
    portfolioRoot.addPosition(position1);
    portfolioRoot.addPosition(position2);

    AnalyticsNode root = new AnalyticsNode.PortfolioNodeBuilder(portfolioRoot).getRoot();
    assertEquals(0, root.getStartRow());
    assertEquals(2, root.getEndRow());
    assertEquals(0, root.getChildren().size());
  }
}
