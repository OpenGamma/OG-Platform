/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.snapshot.UserMarketDataSnapshot;
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
   * @param snapshot  the initialised snapshot, not null
   */
  public MarketDataSnapshotAvailabilityProvider(MarketDataSnapshot snapshot) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    _snapshot = snapshot;
  }
  
  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    if (requirement.getTargetSpecification().getType() == ComputationTargetType.PORTFOLIO_NODE ||
        requirement.getTargetSpecification().getType() == ComputationTargetType.POSITION ||
        requirement.getTargetSpecification().getType() == ComputationTargetType.TRADE) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    if (getSnapshot().query(requirement) != null) {
      return MarketDataAvailability.AVAILABLE;
    }
    if (UserMarketDataSnapshot.getStructuredKey(requirement) != null) {
      return MarketDataAvailability.MISSING;
    }
    return MarketDataAvailability.NOT_AVAILABLE;
  }
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

}
