/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.id.UniqueId;

/**
 * Traverses a portfolio, gathering identifiers corresponding to {@link PortfolioNode}, {@link Position} and {@link Trade} entities. These are targets that may potentially create terminal output
 * requirements from on a view definition.
 */
/* package */class PortfolioIdentifierGatherer extends AbstractPortfolioNodeTraversalCallback {

  private final ConcurrentMap<UniqueId, Boolean> _identifiers = new ConcurrentHashMap<UniqueId, Boolean>();

  public Set<UniqueId> getIdentifiers() {
    return _identifiers.keySet();
  }

  @Override
  public void preOrderOperation(final PortfolioNode portfolioNode) {
    _identifiers.put(portfolioNode.getUniqueId(), Boolean.TRUE);
  }

  @Override
  public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
    if (_identifiers.putIfAbsent(position.getUniqueId(), Boolean.TRUE) == null) {
      for (Trade trade : position.getTrades()) {
        _identifiers.put(trade.getUniqueId(), Boolean.TRUE);
      }
    }
  }

}
