/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FilteringTest {

  private static SimpleTrade sampleTrade(final AtomicInteger attribSource) {
    final SimpleTrade trade = new SimpleTrade();
    trade.addAttribute("Foo", Integer.toString(attribSource.getAndIncrement()));
    return trade;
  }

  private static SimplePosition samplePosition(final AtomicInteger attribSource) {
    final SimplePosition position = new SimplePosition();
    position.addTrade(sampleTrade(attribSource));
    return position;
  }

  private static SimplePortfolioNode samplePortfolioNode(final AtomicInteger attribSource, final int depth) {
    final SimplePortfolioNode node = new SimplePortfolioNode();
    node.setName("Node " + attribSource.getAndIncrement());
    if (depth > 0) {
      node.addChildNode(samplePortfolioNode(attribSource, depth - 1));
      node.addChildNode(samplePortfolioNode(attribSource, depth - 1));
    }
    node.addPosition(samplePosition(attribSource));
    node.addPosition(samplePosition(attribSource));
    return node;
  }

  private static Portfolio samplePortfolio() {
    final AtomicInteger num = new AtomicInteger();
    final SimplePortfolioNode root = new SimplePortfolioNode();
    root.setName("Sample");
    root.addChildNode(samplePortfolioNode(num, 2));
    root.addChildNode(samplePortfolioNode(num, 2));
    return new SimplePortfolio(UniqueId.of("Test", "Sample"), "Sample", root);
  }

  private static void assertShape(final PortfolioNode portfolioNode, final Object[] tree) {
    final int numNodes = (Integer) tree[0];
    final int numPositions = (Integer) tree[1];
    assertEquals(portfolioNode.getChildNodes().size(), numNodes);
    assertEquals(portfolioNode.getPositions().size(), numPositions);
    for (int i = 0; i < numNodes; i++) {
      assertShape(portfolioNode.getChildNodes().get(i), (Object[]) tree[i + 2]);
    }
  }

  private static void print(final Trade trade, final String indent) {
    System.out.println(indent + "TRADE: " + trade.getAttributes().get("Foo"));
  }

  private static void print(final Position position, final String indent) {
    System.out.println(indent + "POSITION: " + position.getUniqueId());
    for (Trade trade : position.getTrades()) {
      print(trade, indent + "  ");
    }
  }

  private static void print(final PortfolioNode portfolioNode, final String indent) {
    System.out.println(indent + "NODE: " + portfolioNode.getName() + "/" + portfolioNode.getUniqueId());
    for (PortfolioNode childNode : portfolioNode.getChildNodes()) {
      print(childNode, indent + "  ");
    }
    for (Position position : portfolioNode.getPositions()) {
      print(position, indent + "  ");
    }
  }

  @Test
  public void testNoFilter() {
    final PortfolioFilter filter = new PortfolioFilter(new FilteringFunction() {

      @Override
      public boolean acceptPortfolioNode(PortfolioNode portfolioNode) {
        return true;
      }

      @Override
      public boolean acceptPosition(Position position) {
        return true;
      }

      @Override
      public String getName() {
        return "No filter";
      }

    });
    final Portfolio portfolio = filter.filter(samplePortfolio());
    //print(portfolio.getRootNode(), "TestNoFilter");
    assertShape(
        portfolio.getRootNode(),
        new Object[] {
            2, 0,
            new Object[] {
                2, 2,
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } },
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } } },
            new Object[] {
                2, 2,
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } },
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } } } });
  }

  @Test
  public void testWithFilter() {
    final PortfolioFilter filter = new PortfolioFilter(new AbstractFilteringFunction("Test") {
      // The default portfolio node filter will remove empty nodes
      @Override
      public boolean acceptPosition(final Position position) {
        for (Trade trade : position.getTrades ()) {
          final int attrib = Integer.parseInt(trade.getAttributes ().get ("Foo"));
          if ((attrib <= 6) || (attrib >= 33)) {
            return false;
          }
        }
        return true;
      }
    });
    final Portfolio portfolio = filter.filter(samplePortfolio());
    //print(portfolio.getRootNode(), "TestPositionFilter");
    assertShape(
        portfolio.getRootNode(),
        new Object[] {
            2, 0,
            new Object[] {
                2, 2,
                new Object[] {
                    1, 2,
                    new Object[] {0, 1 } },
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } } },
            new Object[] {
                1, 0,
                new Object[] {
                    2, 2,
                    new Object[] {0, 2 },
                    new Object[] {0, 2 } } } });
  }

}
