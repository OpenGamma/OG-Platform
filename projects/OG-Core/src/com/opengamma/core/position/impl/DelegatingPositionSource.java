/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of positions that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingPositionSource extends UniqueIdentifierSchemeDelegator<PositionSource> implements PositionSource {

  /**
   * Creates a new instance with a default source of securities.
   * 
   * @param defaultSource  the default source to fall back to, not null
   */
  public DelegatingPositionSource(PositionSource defaultSource) {
    super(defaultSource);
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
  
  

}
