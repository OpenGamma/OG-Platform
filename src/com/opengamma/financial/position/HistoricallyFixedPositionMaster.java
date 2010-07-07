/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.Set;

import javax.time.InstantProvider;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * This PositionMaster retrieves all positions as of a fixed historical date.
 */
public class HistoricallyFixedPositionMaster implements PositionMaster {
  
  private final ManageablePositionMaster _delegate;
  private final InstantProvider _fixTime;
  private final InstantProvider _asViewedAt;
  
  public HistoricallyFixedPositionMaster(ManageablePositionMaster delegate,
      InstantProvider fixTime,
      InstantProvider asViewedAt) {
    ArgumentChecker.notNull(delegate, "Delegate Position Master");
    ArgumentChecker.notNull(fixTime, "Fix Time");
    ArgumentChecker.notNull(asViewedAt, "As Viewed At Time");
    
    _delegate = delegate;
    _fixTime = fixTime;
    _asViewedAt = asViewedAt;
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return _delegate.getPortfolioIds(); // TODO
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    return _delegate.getPortfolio(uid, _fixTime); // TODO
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    return _delegate.getPortfolioNode(uid, _fixTime); // TODO
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    return _delegate.getPosition(uid, _fixTime); // TODO
  }
  
}
