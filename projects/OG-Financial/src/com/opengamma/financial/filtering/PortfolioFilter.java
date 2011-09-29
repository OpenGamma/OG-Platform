/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import java.util.Collection;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;

/**
 * A filter of portfolios. One or more filters are applied to a portfolio to remove trades, positions, or
 * nodes from it.
 */
public class PortfolioFilter implements FilteringFunction {

  private static final UniqueIdSupplier s_syntheticIdentifiers = new UniqueIdSupplier("PortfolioFilter");

  private final FilteringFunction[] _filteringFunctions;

  public PortfolioFilter(final FilteringFunction filteringFunction) {
    _filteringFunctions = new FilteringFunction[] {filteringFunction };
  }

  public PortfolioFilter(final FilteringFunction... filteringFunctions) {
    _filteringFunctions = new FilteringFunction[filteringFunctions.length];
    System.arraycopy(filteringFunctions, 0, _filteringFunctions, 0, filteringFunctions.length);
  }

  public PortfolioFilter(final Collection<FilteringFunction> filteringFunctions) {
    _filteringFunctions = filteringFunctions.toArray(new FilteringFunction[filteringFunctions.size()]);
  }

  private static UniqueId createSyntheticIdentifier() {
    return s_syntheticIdentifiers.get();
  }

  private FilteringFunction[] getFilteringFunctions() {
    return _filteringFunctions;
  }

  @Override
  public boolean acceptPosition(final Position position) {
    for (FilteringFunction function : getFilteringFunctions()) {
      if (!function.acceptPosition(position)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean acceptPortfolioNode(final PortfolioNode portfolioNode) {
    for (FilteringFunction function : getFilteringFunctions()) {
      if (!function.acceptPortfolioNode(portfolioNode)) {
        return false;
      }
    }
    return true;
  }

  protected SimplePortfolioNode filter(final PortfolioNode inputPortfolioNode) {
    final SimplePortfolioNode newPortfolioNode = new SimplePortfolioNode();
    newPortfolioNode.setUniqueId(createSyntheticIdentifier());
    newPortfolioNode.setName(inputPortfolioNode.getName());
    for (Position position : inputPortfolioNode.getPositions()) {
      if (acceptPosition(position)) {
        newPortfolioNode.addPosition(position);
      }
    }
    for (PortfolioNode portfolioNode : inputPortfolioNode.getChildNodes()) {
      final SimplePortfolioNode filteredPortfolioNode = filter(portfolioNode);
      if (acceptPortfolioNode(filteredPortfolioNode)) {
        filteredPortfolioNode.setParentNodeId(newPortfolioNode.getUniqueId());
        newPortfolioNode.addChildNode(filteredPortfolioNode);
      }
    }
    return newPortfolioNode;
  }

  public Portfolio filter(final Portfolio inputPortfolio) {
    return new SimplePortfolio(UniqueId.of(inputPortfolio.getUniqueId().getScheme(), buildPortfolioName(inputPortfolio.getUniqueId().getValue())), buildPortfolioName(inputPortfolio.getName()),
        filter(inputPortfolio.getRootNode()));
  }

  protected String buildPortfolioName(final String existingName) {
    return existingName + " filtered by " + getName();
  }

  @Override
  public String getName() {
    StringBuilder name = new StringBuilder();
    boolean comma = false;
    for (FilteringFunction function : getFilteringFunctions()) {
      if (comma) {
        name.append(", ");
      } else {
        comma = true;
      }
      name.append(function.getName());
    }
    return name.toString();
  }

}
