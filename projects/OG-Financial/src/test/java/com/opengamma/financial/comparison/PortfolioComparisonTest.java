/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioComparisonTest extends AbstractTest {

  public void testComparison() {
    final SimplePortfolioNode nodeA = new SimplePortfolioNode();
    nodeA.addPosition(createPosition("1", 10, createRawSecurity("1", 42), null, null, null, null));
    nodeA.addPosition(createPosition("3", 10, createRawSecurity("3", 42), null, null, null, null));
    final SimplePortfolio portfolioA = new SimplePortfolio("A", nodeA);
    final SimplePortfolioNode nodeB = new SimplePortfolioNode();
    nodeB.addPosition(createPosition("2", 10, createRawSecurity("2", 42), null, null, null, null));
    nodeB.addPosition(createPosition("3", 10, createRawSecurity("3", 42), null, null, null, null));
    final SimplePortfolio portfolioB = new SimplePortfolio("B", nodeB);
    final PortfolioComparator comparator = new PortfolioComparator(OpenGammaFudgeContext.getInstance());
    final PortfolioComparison result = comparator.compare(portfolioA, portfolioB);
    assertEquals(result.getOnlyInFirst().size(), 1);
    assertEquals(result.getOnlyInSecond().size(), 1);
    assertEquals(result.getIdentical().size(), 1);
    assertEquals(result.getOnlyInFirstPortfolio().getRootNode().getPositions().size(), 1);
    assertEquals(result.getOnlyInSecondPortfolio().getRootNode().getPositions().size(), 1);
    assertEquals(result.getCommonPortfolio().getRootNode().getPositions().size(), 1);
  }

}
