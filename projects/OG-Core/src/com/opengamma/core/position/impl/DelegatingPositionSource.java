/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of positions that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different sources and presented through a single change manager.
 */
public class DelegatingPositionSource extends UniqueIdentifierSchemeDelegator<PositionSource> implements PositionSource {

  private final ChangeManager _changeManager;
  
  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingPositionSource(PositionSource defaultSource) {
    super(defaultSource);
    _changeManager = defaultSource.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingPositionSource(PositionSource defaultSource, Map<String, PositionSource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    
    // REVIEW jonathan 2011-08-03 -- this assumes that the delegating source lasts for the lifetime of the engine as we
    // never detach from the underlying change managers.
    changeManager.addChangeManager(defaultSource.changeManager());
    for (PositionSource source : schemePrefixToSourceMap.values()) {
      changeManager.addChangeManager(source.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPortfolio(uid);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPosition(uid);
  }

  @Override
  public Trade getTrade(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getTrade(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
