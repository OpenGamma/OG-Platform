/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.Collection;
import java.util.LinkedList;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;

/**
 * Provides comparison operations between {@link Portfolio} objects.
 */
public class PortfolioComparator extends PositionSetComparator {

  public PortfolioComparator(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  private static Collection<Position> getFlattenedPositions(final Portfolio portfolio) {
    final Collection<Position> positions = new LinkedList<Position>();
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {
      @Override
      public void preOrderOperation(final Position position) {
        positions.add(position);
      }
    }).traverse(portfolio.getRootNode());
    return positions;
  }

  public PortfolioComparison compare(final Portfolio first, final Portfolio second) {
    return new PortfolioComparison(compare(getFlattenedPositions(first), getFlattenedPositions(second)), first.getName(), second.getName());
  }

}
