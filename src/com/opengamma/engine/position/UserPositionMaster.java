/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * A {@link PositionMaster} which accepts temporary user {@link Portfolio}s, kept alive by heartbeats.
 * 
 * @param <O>  the type of the owner to be associated with each user {@link Portfolio}
 */
public interface UserPositionMaster<O> extends PositionMaster {
  
  /**
   * Adds a new portfolio to the master, associated with the specified owner. The owner must provide heartbeats in
   * order to guarantee that the portfolio will remain available through the master.
   * 
   * @param owner  the owner of the portfolio, not null
   * @param portfolio  the portfolio to add, not null
   */
  void addPortfolio(O owner, Portfolio portfolio);
  
  /**
   * Indicates that the specified owner is alive. If the owner is not known to the {@link PositionMaster} then this
   * has no effect.
   * 
   * @param owner  the owner, not null
   * @return <code>false</code> if the specified owner is not known to the {@link PositionMaster} (or has already been
   *         expired), <code>true</code> otherwise.
   */
  boolean heartbeat(O owner);

}
