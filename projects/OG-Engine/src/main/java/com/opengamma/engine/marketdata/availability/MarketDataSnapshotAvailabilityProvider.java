/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link MarketDataAvailabilityProvider} which uses an underlying {@link MarketDataSnapshot} as the definition of whether a requirement can be satisfied.
 */
public class MarketDataSnapshotAvailabilityProvider implements MarketDataAvailabilityProvider {

  // PLAT-3044 Delete this; the MarketDataSnapshot does not have enough state to do the resolution just from it. The only place it is used is in
  // conjunction with UserMarketDataSnapshot which is constructed with more capabilities for value resolution.

  private final MarketDataSnapshot _snapshot;

  /**
   * Constructs an instance.
   *
   * @param snapshot the initialised snapshot, not null
   */
  public MarketDataSnapshotAvailabilityProvider(final MarketDataSnapshot snapshot) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    _snapshot = snapshot;
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    // [PLAT-3044] Do this properly
    /*if (desiredValue.getTargetReference().getType() == ComputationTargetType.PORTFOLIO_NODE ||
        desiredValue.getTargetReference().getType() == ComputationTargetType.POSITION ||
        desiredValue.getTargetReference().getType() == ComputationTargetType.TRADE) {
      return null;
    }
    final ComputedValue snapshotValue = getSnapshot().query(desiredValue);
    if (snapshotValue != null) {
      return snapshotValue.getSpecification();
    }
    // Andrew 2012-10-05 -- No one in the office can remember why this logic to suppress graph construction was needed, or exactly
    // what the suppression logic should have been.
    //if (UserMarketDataSnapshot.getStructuredKey(requirement) != null) {
    //throw new MarketDataNotSatisfiableException(requirement);
    //}
    return null;*/
    throw new UnsupportedOperationException("[PLAT-3044] MarketDataSnapshot doesn't have enough info. Can we construct from a more thorough type for this case?");
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

}
