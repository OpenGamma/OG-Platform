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
import com.opengamma.id.UniqueId;

/**
 * Provides comparison operations between {@link Portfolio} objects.
 */
public class PortfolioComparator extends PositionSetComparator {

  public PortfolioComparator(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  public static Collection<Position> getFlattenedPositions(final Portfolio portfolio) {
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
    UniqueId firstId = first.getUniqueId();
    UniqueId secondId = second.getUniqueId();
    String firstName;
    String secondName;

    // if they are two versions of the same portfolio the names need to include the version otherwise the generated
    // portfolio name won't make much sense
    if (firstId != null && secondId != null && firstId.getObjectId().equals(secondId.getObjectId())) {
      firstName = first.getName() + " (version " + firstId.getVersion() + ")";
      secondName = second.getName() + " (version " + secondId.getVersion() + ")";
    } else {
      firstName = first.getName();
      secondName = second.getName();
    }
    return new PortfolioComparison(compare(getFlattenedPositions(first), getFlattenedPositions(second)), firstName, secondName);
  }

}
