/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.UnsatisfiableDependencyGraphException;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.UserMarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link MarketDataAvailabilityProvider} which uses an underlying {@link MarketDataSnapshot} as  
 * the definition of whether a requirement can be satisfied.
 */
public class MarketDataSnapshotAvailabilityProvider implements MarketDataAvailabilityProvider {

  private final MarketDataSnapshot _snapshot;
  
  /**
   * Constructs an instance.
   * 
   * @param snapshot  the initialised snapshot, not {@code null}
   */
  public MarketDataSnapshotAvailabilityProvider(MarketDataSnapshot snapshot) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    _snapshot = snapshot;
  }
  
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if (requirement.getTargetSpecification().getType() == ComputationTargetType.PORTFOLIO_NODE ||
        requirement.getTargetSpecification().getType() == ComputationTargetType.POSITION ||
        requirement.getTargetSpecification().getType() == ComputationTargetType.TRADE) {
      return false;
    }
    if (getSnapshot().query(requirement) != null) {
      return true;
    }
    if (UserMarketDataSnapshot.getStructuredKey(requirement) != null) {
      throw new UnsatisfiableDependencyGraphException(requirement); //PLAT-1419 make sure this structured data is not resolved another way 
    }
    return false;
  }
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

}
