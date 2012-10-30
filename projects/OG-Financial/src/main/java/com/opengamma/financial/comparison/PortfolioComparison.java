/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.Collection;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;

/**
 * Represents the result of a portfolio comparison operation, providing:
 * <ul>
 * <li>The intersection of identical positions</li>
 * <li>The positions only present in the first portfolio</li>
 * <li>The positions only present in the second portfolio</li>
 * <li>The positions that exist in both but have changed from the first to the second</li>
 * </ul>
 */
public class PortfolioComparison extends PositionSetComparison {

  /**
   * First portfolio name.
   */
  private final String _leftName;

  /**
   * Second portfolio name.
   */
  private final String _rightName;

  protected PortfolioComparison(final PositionSetComparison underlying, final String leftName, final String rightName) {
    super(underlying);
    _leftName = leftName;
    _rightName = rightName;
  }

  public Portfolio getOnlyInFirstPortfolio() {
    return createPortfolio("Positions of " + getLeftName() + " not in " + getRightName(), getOnlyInFirst());
  }

  public Portfolio getOnlyInSecondPortfolio() {
    return createPortfolio("Positions of " + getRightName() + " not in " + getLeftName(), getOnlyInSecond());
  }

  public Portfolio getCommonPortfolio() {
    return createPortfolio("Common positions of " + getLeftName() + " and " + getRightName(), getIdentical());
  }

  private String getLeftName() {
    return _leftName;
  }

  private String getRightName() {
    return _rightName;
  }

  private Portfolio createPortfolio(final String name, final Collection<Position> positions) {
    final SimplePortfolio portfolio = new SimplePortfolio(name);
    final SimplePortfolioNode rootNode = new SimplePortfolioNode(name);
    rootNode.addPositions(positions);
    portfolio.setRootNode(rootNode);
    return portfolio;
  }

}
