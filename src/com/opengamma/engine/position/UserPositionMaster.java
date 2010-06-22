/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import com.opengamma.id.UniqueIdentifier;

/**
 * A {@link PositionMaster} which accepts temporary user {@link Portfolio}s, kept alive by heartbeats.
 */
public interface UserPositionMaster extends PositionMaster {
  
  /**
   * Adds a new portfolio to the master, associated with the specified owner. The owner must provide heartbeats in
   * order to guarantee that the portfolio will remain available through the master.
   * 
   * @param ownerId  the ID of the portfolio's owner, not null
   * @param portfolio  the portfolio to add, not null
   */
  void addPortfolio(UniqueIdentifier ownerId, Portfolio portfolio);
  
  /**
   * Indicates that the specified owner is alive. If the owner is not known to the {@link PositionMaster} then this
   * has no effect.
   * 
   * @param ownerId  the ID of the portfolio's owner, not null
   * @return <code>false</code> if the specified owner is not known to the {@link PositionMaster} (or has already been
   *         expired), <code>true</code> otherwise.
   */
  boolean heartbeat(UniqueIdentifier ownerId);

}
