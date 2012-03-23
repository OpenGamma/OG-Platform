/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;

import java.util.Collection;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.financial.comparison.PortfolioComparator;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class ExamplePositionReader extends AbstractExampleTool {

  @Test
  public void test() throws Exception {
    System.err.println("running doRun");
    initAndRun(new String[] {});
    doRun();
  }
  
  
  @Override
  protected void doRun() throws Exception {
    PositionSource positionSource = getToolContext().getPositionSource();
    Portfolio portfolio = positionSource.getPortfolio(UniqueId.parse("DbPrt~1000"));
    Collection<Position> flattenedPositions = PortfolioComparator.getFlattenedPositions(portfolio);
    for (Position position : flattenedPositions) {
      System.err.println(position);
    }
  }
  
  

}
