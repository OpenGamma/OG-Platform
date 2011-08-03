/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of positions that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingPositionSource extends UniqueIdSchemeDelegator<PositionSource> implements PositionSource {

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingPositionSource(PositionSource defaultSource) {
    super(defaultSource);
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingPositionSource(PositionSource defaultSource, Map<String, PositionSource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId).getPortfolio(uniqueId);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId).getPortfolioNode(uniqueId);
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId).getPosition(uniqueId);
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId).getTrade(uniqueId);
  }

}
