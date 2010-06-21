/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Set;

import com.opengamma.id.UniqueIdentifier;

/**
 * Provides access to a remote {@link UserPositionMaster}.
 */
public class RemoteUserPositionMaster implements UserPositionMaster<UniqueIdentifier> {

  @Override
  public void addPortfolio(UniqueIdentifier owner, Portfolio portfolio) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean heartbeat(UniqueIdentifier owner) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
