/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;

/**
 * Tests that {@link AnalyticsNode.PortfolioNodeBuilder} creates nodes that match a portfolio structure.
 */
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
    assertEquals(1, child.getStartRow());
    assertEquals(3, child.getEndRow());
    assertTrue(child.getChildren().isEmpty());
  }

  @Test
  public void deeplyNested() {
    /*
    0  root
    1   |_child1
    2   |  |_child2
    3   |  | |_child3
    4   |  | | |_pos
    5   |  | | |_pos
    6   |  | |_pos
    7   |  | |_pos
    8   |  |_pos
    9   |  |_pos
    10  |_pos
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
    assertEquals(1, child1.getStartRow());
    assertEquals(9, child1.getEndRow());
    assertEquals(1, child1.getChildren().size());
    AnalyticsNode child2 = child1.getChildren().get(0);
    assertEquals(2, child2.getStartRow());
    assertEquals(7, child2.getEndRow());
    assertEquals(1, child2.getChildren().size());
    AnalyticsNode child3 = child2.getChildren().get(0);
    assertEquals(3, child3.getStartRow());
    assertEquals(5, child3.getEndRow());
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
}
